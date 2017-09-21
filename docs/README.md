Documentation
=============

Based directly upon [DF-documentation-poc](https://github.com/magenta-aps/df-documentation-poc).
See this repository for more details.

Stripped to solely utilize sphinx and javasphinx.


## Building the documentation

SSH into your virtual machine using 

    vagrant ssh

You should already be in the Python Virtual Environment, indicated by the 
`(venv)` terminal prefix, and you should already be in the `/vagrant/` folder.
So to build the docs, it should suffice to run:

    ./gen_doc.sh

Your new docoumentation is generated at `vagrant/docs/output`. 


## Serving the documentation as a website

The `gen_doc.sh` script generates a static HTML website with the documentation. 

To use a web browser to view the site, navigate to the root dir of the
documentation site, and start a server:

    ./run_dev_server.sh

Now open a browser on your host machine and point it to `localhost:8000`.
It should display the documentation site.


## Translations

The documentation site(s) is translated in the Sphinx build process. 
More info at: http://www.sphinx-doc.org/en/stable/intl.html

You should be able to find language files for Greenlandic (kl), Danish (da), and English (en) i the `source/locales` folder.

**If you're a translater,** you'll want to edit all the `.po` files in the directory containing your language files. When done, just run another build with your translation as an argument. 

Say you want to translate the docs from Danish to Greenlandic (kl):

* Find the language files for Greenlandic in `source/locales/kl/LC_MESSAGES`
* Open each `.po` file and add the corresponding Greenlandic paragraphs in each empty `msgstr`. Say for example:

    #: ../../../index.rst:12
    msgid "Velkommen til den offentlige dokumentation for Grønlands Datafordeler."
    msgstr "[Greenlandic translation goes here]"

* Run a new build with the `kl` option

    ./gen_doc.sh kl

* If all goes well, a copy of the site with Greenlandic translations will be available at `output/html/kl/`


### Creating translation templates

If you need to create new translation template files, use gettext to crete a new set of `.pot files`. 
For example, if you want to create new files for a Spanish translation:

    sphinx-build -b gettext docs/source docs/source/locales/es/LC_MESSAGES/

Then rename the `.pot` files to `.po` and translate as instructed above.
