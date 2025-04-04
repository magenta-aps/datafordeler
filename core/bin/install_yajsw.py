import glob
import os
import re
import ssl
import urllib2
import zipfile

BASE_DIR = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
BIN_DIR = os.path.join(BASE_DIR, "bin")
DEST_DIR = os.path.join(BASE_DIR, "yajsw")


def download(url, file_name=None):
    if file_name is None:
        file_name = url.split('/')[-1]
        file_name = os.path.join(BASE_DIR, file_name)

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


def install_yajsw():
    download(
        "https://downloads.sourceforge.net" +
        "/project/yajsw/yajsw/yajsw-stable-12.09/" +
        "yajsw-stable-12.09.zip"
    )

    ZIP_FILE = os.path.join(BASE_DIR, "yajsw-stable-12.09.zip")

    zip_archive = zipfile.ZipFile(ZIP_FILE, 'r', allowZip64=True)

    print "Unpacking yajsw-stable-12.09.zip"
    zip_archive.extractall(path=BASE_DIR)

    os.rename(
        os.path.join(BASE_DIR, "yajsw-stable-12.09"),
        os.path.join(BASE_DIR, "yajsw")
    )

    DEST_CONF_FILE = os.path.join(BASE_DIR, "yajsw", "conf", "wrapper.conf")
    conf_contents = expand_template()
    f = open(DEST_CONF_FILE, 'w')
    f.write(conf_contents)
    f.close()

    print ""
    print "You can now set up a windows service by running the files in"
    print "  " + os.path.join(BASE_DIR, "yajsw", "bat")
    print "with Administrator privileges."
    print ""
    print "You should test that the application can run using %s " % (
        "runConsole.bat"
    )
    print "and then use %s to install it." % ("installService.bat")
    print ""


def escape_conf_path(path):
    return re.sub(r'\\', '\\\\\\\\\\\\\\\\', path)


def expand_template():
    CONF_FILE = os.path.join(BASE_DIR, "conf", "yajsw.conf.template")

    f = open(CONF_FILE, 'r')
    template = f.read()
    f.close()

    java_exes = glob.glob("C:\\Program F*\\Java\\*\\bin\\java.exe")
    if len(java_exes) == 0:
        raise Exception("No java installations found")

    java_exe = None
    if len(java_exes) == 1:
        java_exe = java_exes[0]
    else:
        print ""
        while java_exe is None:
            print "Choose which java.exe to use"
            nr = 1
            for x in java_exes:
                print "%s) %s" % (nr, x)
                nr = nr + 1

            sel = raw_input("Choice (default: 1)> ")

            try:
                java_exe = java_exes[int(sel if sel else 1)]
            except:
                pass

    username = None
    while username is None:
        print ""
        print "Input username to run service as in format <domain>\\<username>"
        x = raw_input("Input username> ")
        if re.match(r'\S+\\\S+', x):
            username = x

    password = None
    while password is None:
        print ""
        print "Input password for user %s" % username
        x = raw_input("Input password> ")
        if x:
            password = x

    replace_map = {
        'BASE_DIR': escape_conf_path(BASE_DIR + "\\"),
        'USERNAME': re.sub(r'\\', '\\\\\\\\', username),
        'PASSWORD': password,
        'JAVA_EXE': escape_conf_path(java_exe)
    }

    print ""
    print "Going to expand configuration using the following settings."
    print ""
    for x in replace_map:
        print "%s: %s" % (x, replace_map[x])
    print ""
    print "Note that paths should have four backslashes where you " + \
        "normally would have one and username should have two backslashes " + \
        "instead of one."
    print ""
    print "Is this ok?"
    x = raw_input("Continue? (Y/n)>")
    if not x or re.match(r'^[yj]$', x.lower()):
        pass
    else:
        print "Ok, exiting"
        exit()

    def re_replace(matchobj):
        key = matchobj.group(1).upper()
        if key in replace_map:
            return replace_map[key]
        else:
            return matchobj.group(0)

    return re.sub(r'\{\{\s*([^\s}]+)\s*\}\}', re_replace, template)


if __name__ == '__main__':

    if os.path.exists(DEST_DIR):
        print "YAJSW already installed in %s, remove to reinstall" % DEST_DIR
    else:
        install_yajsw()
