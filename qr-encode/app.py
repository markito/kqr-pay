#!/bin/python
#########
## QRGenerator
#########

from flask import Flask, send_file
from flask import request
from io import BytesIO
from money.money import Money
from money.currency import Currency
from money.money import Money
import traceback
import logging
import qrcode
import signal

app = Flask(__name__)
DEFAULT_ID = "-1111111111"

def _serve_pil_image(pil_img):
    img_io = BytesIO()
    pil_img.save(img_io, 'JPEG', quality=70)
    img_io.seek(0)
    return send_file(img_io, mimetype='image/jpeg')

@app.route('/encode', methods=['GET', 'POST'])
def encode():
    source = request.args.get('source', default = "Red Hat Summit 2019", type = str)
    id = request.args.get('id', default = DEFAULT_ID, type = str)
    amount = request.args.get('amount', default = "0.0", type = str)
    try:
        if (id == DEFAULT_ID):
            img = qrcode.make(source + ";" + id + ";" + amount)
            return _serve_pil_image(img)
        else: 
            money_amount = Money(amount, Currency.USD)
            img = qrcode.make(source + ";" + id + ";" + money_amount.format('en_US'))
        return _serve_pil_image(img)
    except Exception as e:
        logging.error(traceback.format_exc())
        return "Error generating QR code."

def signal_term_handler(signal, frame):
    logging.warning('got SIGTERM')
    sys.exit(0)

if __name__ == "__main__":
    signal.signal(signal.SIGTERM, signal_term_handler)
    app.run(host='0.0.0.0', port=8080)
