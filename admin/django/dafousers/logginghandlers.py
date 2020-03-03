import logging.handlers
import os


class NoAdminNTEventLogHandler(logging.handlers.NTEventLogHandler):
    """
    Modified version of the NTEventLogHandler that does not register the
    logger in the registry unless told to do so. This allows running the
    handler without administrator privileges.
    """
    def __init__(self, appname, dllname=None, logtype="Application",
                 add_to_registry=False):
        logging.Handler.__init__(self)
        try:
            import win32evtlogutil, win32evtlog
            self.appname = appname
            self._welu = win32evtlogutil
            if not dllname:
                dllname = os.path.split(self._welu.__file__)
                dllname = os.path.split(dllname[0])
                dllname = os.path.join(dllname[0], r'win32service.pyd')
            self.dllname = dllname
            self.logtype = logtype
            if(add_to_registry):
                self._welu.AddSourceToRegistry(appname, dllname, logtype)
            self.deftype = win32evtlog.EVENTLOG_ERROR_TYPE
            self.typemap = {
                logging.DEBUG   : win32evtlog.EVENTLOG_INFORMATION_TYPE,
                logging.INFO    : win32evtlog.EVENTLOG_INFORMATION_TYPE,
                logging.WARNING : win32evtlog.EVENTLOG_WARNING_TYPE,
                logging.ERROR   : win32evtlog.EVENTLOG_ERROR_TYPE,
                logging.CRITICAL: win32evtlog.EVENTLOG_ERROR_TYPE,
         }
        except ImportError:
            print("The Python Win32 extensions for NT (service, event "\
                        "logging) appear not to be available.")
            self._welu = None
