

class ModelDatabaseRouter(object):
    """Allows each model to set its own destiny"""

    def db_for_read(self, model, **hints):
        # Specify target database with field in_db in model's Meta class
        return getattr(model._meta, 'database', None)

    def db_for_write(self, model, **hints):
        # Specify target database with field in_db in model's Meta class
        return getattr(model._meta, 'database', None)

    def allow_syncdb(self, db, model):
        # Specify target database with field in_db in model's Meta class
        must_match = getattr(model._meta, 'database', 'default')
        return must_match == db
