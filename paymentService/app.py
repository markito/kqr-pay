#!flask/bin/python
import stripe
from flask import Flask, flash, request, redirect, url_for
import signal
import sys

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

@app.route('/', methods=['POST'])
def payment(): 
    req_data = request.get_json()
    
    if (_validateRequest(req_data)):
        orderNumber = req_data['orderNumber']
        amount = req_data['amount']
        email = "serverless-interest@redhat.com"
        return _stripePayment(amount, email)
    else:
        return False

def _validateRequest(req_data):
    if ('orderNumber' in req_data and 'amount' in req_data):
        return True
    else:
        return False

if __name__ == "__main__":
    signal.signal(signal.SIGTERM, signal_term_handler)
    app.run(host='0.0.0.0', port=8080)

