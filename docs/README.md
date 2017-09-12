Documentation
=============

Based directly upon [DF-documentation-poc](https://github.com/magenta-aps/df-documentation-poc).
See this repository for more details.

Stripped to solely utilize sphinx and javasphinx.


## Building the documentation

SSH into your virtual machine using 

    vagrant ssh

Then become root, start Python Virtual Environment, and build the docs:

    sudo su
    cd /vagrant/
    source ~/venv/bin/activate
    ./gen_doc.sh

Your new docoumentation is generated at `vagrant/docs/output`. 


## Serving the documentation as a website

The gen_doc script generates a static HTML website with the documentation. 

To use a web browser to view the site, navigate to the root dir of the documentation site and start a server:

    cd /vagrant/docs/output/html
    python -m SimpleHTTPServer

Now open a browser on your host machine and point it to `localhost:8000`. It should display the documentation site.
