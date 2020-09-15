#!flask/bin/python
import stripe
import logging
from logging.config import dictConfig
from flask import Flask, flash, request, redirect, url_for
import signal
import json
import sys

dictConfig({
    'version': 1,
    'formatters': {'default': {
        'format': '[%(asctime)s] %(levelname)s in %(module)s: %(message)s',
    }},
    'handlers': {'wsgi': {
        'class': 'logging.StreamHandler',
        'formatter': 'default'
    }},
    'root': {
        'level': 'INFO',
        'handlers': ['wsgi']
    }
})


# Set your secret key: remember to change this to your live secret key in production
# See your keys here: https://dashboard.stripe.com/account/apikeys
stripe.api_key = 'sk_test_4eC39HqLyjWDarjtT1zdp7dc'

def _stripePayment(inputAmount=0, email='user@example.com'):
    response = stripe.PaymentIntent.create(
        amount=inputAmount,
        currency='usd',
        payment_method_types=['card'],
        receipt_email=email,
    )
    return response 

def signal_term_handler(signal, frame):
    logging.warning('Received SIGTERM')
    sys.exit(0)

app = Flask(__name__)
# @app.route('/')
# def index():
#     return "Hello, World! This is the payment service. "

@app.before_request
def log_request_info():
    app.logger.info('Headers: %s', request.headers)
    app.logger.info('Body: %s', request.get_data())

@app.route('/', methods=['POST'])
def payment(): 
    if request.content_type != 'application/json':
        req_data = json.loads(request.get_data().decode("utf-8"))
    else:
        req_data = request.get_json()

    if (_validateRequest(req_data)):
        orderNumber = req_data['orderNumber']
        amount = req_data['amount']
        email = "serverless@redhat.com"
        try: 
            result = _stripePayment(amount, email)
            app.logger.info('Transaction successfully processed. %s' % (result.id))
            return result
        except Exception as error: 
            app.logger.error("{'Transaction failed': '%s'}" % (error) )
            return "{'errorMessage': '%s'}" % (error) 
    else:
        return "{'errorMessage': 'Invalid payload for payment request. %s'}" % (req_data)

def _validateRequest(req_data):
    if ('orderNumber' in req_data and 'amount' in req_data):
        return True
    else:
        return False

if __name__ == "__main__":
    signal.signal(signal.SIGTERM, signal_term_handler)
    app.run(host='0.0.0.0', port=8080)

