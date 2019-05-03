# -*- coding: utf-8 -*-
#!/bin/python
#########
## QRReader Payment app
#########
from money.money import Money
from flask import Flask, flash, request, redirect, url_for
from pyzbar.pyzbar import decode
from PIL import Image, ImageEnhance


ALLOWED_EXTENSIONS = set(['png', 'jpg', 'jpeg', 'gif'])
app = Flask(__name__)

def allowed_file(filename):
    return '.' in filename and \
           filename.rsplit('.', 1)[1].lower() in ALLOWED_EXTENSIONS

'''
    Return data field value from QRcode
'''
def _decode(image):
    local_image = Image.open(image)
    local_image = ImageEnhance.Contrast(local_image).enhance(4.0)
    decodedObjects = decode(local_image)
    # only for demo
    for obj in decodedObjects:
        print('Type : ', obj.type)
        print('Data : ', obj.data,'\n')  
    if (decodedObjects[0].data):
        return decodedObjects[0].data 
    else:
        flash("No data found on this QRcode.")


@app.route('/', methods=['GET', 'POST'])
def upload_file():
    if request.method == 'POST':
        # check if the post request has the file part
        if 'file' not in request.files:
            flash('No file part')
            return redirect(request.url)
        file = request.files['file']

        if file and allowed_file(file.filename):
            return _decode(file)
    # don't this at home - use templates
    return '''
    <!doctype html>
    <title>Payment System - Upload the QR Code</title>
    <link rel="stylesheet" href="https://stackpath.bootstrapcdn.com/bootstrap/4.3.1/css/bootstrap.min.css" integrity="sha384-ggOyR0iXCbMQv3Xipma34MD+dH/1fQ784/j6cY/iJTQUOhcWr7x9JvoRxT2MZw1T" crossorigin="anonymous">
    <h1>QR Payment System 💸💳</h1>
    <form method=post enctype=multipart/form-data>
      <input type=file name=file accept="image/*;capture=camera">
      <input type=submit value="Pay!">
    </form>
    '''

if __name__ == "__main__":
    app.run(host='0.0.0.0', port=8080)
