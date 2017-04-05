# The contents of this file are subject to the Mozilla Public License
# Version 2.0 (the "License"); you may not use this file except in
# compliance with the License. You may obtain a copy of the License at
#    http://www.mozilla.org/MPL/
#
# Software distributed under the License is distributed on an "AS IS" basis,
# WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
# for the specific language governing rights and limitations under the
# License.
#
# Copyright 2015 Magenta Aps
#
import os
import urllib2
import zipfile
import tempfile
import time
import platform
import subprocess
import ssl
import shutil

BASE_DIR = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
BIN_DIR = os.path.join(BASE_DIR, "bin")


def download(url):
    file_name = url.split('/')[-1]
    file_name = os.path.join(BASE_DIR, file_name);

    if os.path.exists(file_name):
        print "Using existing version of %s for %s" % (file_name, url)
        print "Delete it if you wish to re-download"
        return file_name

    ctx = ssl.create_default_context()
    ctx.check_hostname = False
    ctx.verify_mode = ssl.CERT_NONE

    u = urllib2.urlopen(url, context=ctx)
    f = open(file_name, 'wb')
    meta = u.info()
    file_size = int(meta.getheaders("Content-Length")[0])
    print "Downloading: %s Bytes: %s" % (file_name, file_size)

    file_size_dl = 0
    block_sz = 8192
    while True:
        buffer = u.read(block_sz)
        if not buffer:
            print ""
            break

        file_size_dl += len(buffer)
        f.write(buffer)
        status = r"%10d  [%3.2f%%]" % (
            file_size_dl,
            file_size_dl * 100. / file_size
        )
        status = status + chr(8)*(len(status)+1)
        print status,

    f.close()
    return file_name


if __name__ == '__main__':

    print "initial_setup.py done"