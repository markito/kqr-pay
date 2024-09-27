# -*- coding: utf-8 -*-
#!/bin/python
#########
## QR code generator for in-store kiosk
#########

from flask import Flask, send_file
from flask import request
from io import BytesIO
import traceback
import logging
import qrcode
import signal
import sys
import json
import uuid

app = Flask(__name__)

def _serve_pil_image(pil_img):
    img_io = BytesIO()
    pil_img.save(img_io, 'JPEG', quality=70)
    img_io.seek(0)
    return send_file(img_io, mimetype='image/jpeg')


@app.route('/', methods=['GET', 'POST'])
def encode():
    # request params to dict
    dataFromURL = request.args.to_dict()
    dataFromURL.update( {'system' : "Demo OpenShift Serverless and Pipelines Tech Ready"} )
    dataFromURL.update( {'orderNumber' : str(uuid.uuid1())} )
    # transaction total
    try:
        img = qrcode.make(json.dumps(dataFromURL))
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
