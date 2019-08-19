FROM centos:latest 

LABEL maintainer="markito@redhat.com"

RUN yum -y update && yum install -y epel-release && yum -y update && yum install -y qrencode python36 python36-libs python36-devel python36-pip python-pip zbar pyzbar && yum clean all

COPY ./ ./app
WORKDIR ./app
RUN python3 --version && pip3 install -r requirements.txt
ENTRYPOINT /usr/bin/python3 app.py
