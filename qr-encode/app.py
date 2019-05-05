# -*- coding: utf-8 -*-
#!/bin/python
#########
## QR code generator for in-store kiosk
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
import sys

app = Flask(__name__)
DEFAULT_ID = "-1111111111"

def _serve_pil_image(pil_img):
    img_io = BytesIO()
    pil_img.save(img_io, 'JPEG', quality=70)
    img_io.seek(0)
    return send_file(img_io, mimetype='image/jpeg')

@app.route('/encode', methods=['GET', 'POST'])
def encode():
    # origin location for the transaction
    source = request.args.get('source', default = "Red Hat Summit 2019", type = str)
    # transaction id
    txId = request.args.get('id', default = DEFAULT_ID, type = str)
    amount = request.args.get('amount', default = "0.0", type = str)
    # transaction total
    try:
        if (txId == DEFAULT_ID):
            img = qrcode.make(source + ";" + txId + ";" + amount)
            return _serve_pil_image(img)
        else: 
            money_amount = Money(amount, Currency.USD)
            img = qrcode.make(source + ";" + txId + ";" + money_amount.format('en_US'))
        return _serve_pil_image(img)
    except Exception as e:
        logging.error(traceback.format_exc())
        return "Error generating QR code for this transaction."

def signal_term_handler(signal, frame):
    logging.warning('Received SIGTERM')
    sys.exit(0)

if __name__ == "__main__":
    signal.signal(signal.SIGTERM, signal_term_handler)
    app.run(host='0.0.0.0', port=8080)
