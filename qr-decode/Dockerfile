FROM centos:latest 

LABEL maintainer="markito@redhat.com"

RUN yum -y update && yum install -y epel-release && \
 yum -y update

RUN yum install -y zbar-devel qrencode gcc python36 python36-devel && \ 
 dnf install zbar-devel && \
 yum clean all

COPY ./ ./app
WORKDIR ./app
RUN python3 --version && pip3 install -r requirements.txt
ENTRYPOINT /usr/bin/python3 app.py
