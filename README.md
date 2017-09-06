Datafordeler
============

Meta repository for the datafordeler project.

Also main repository for documentation of the project.
For specific documentation about the documentation, see [This README](docs/README.md)

To clone this repository recursively, please run:

     ssh-agent bash -c 'ssh-add; git clone git@github.com:magenta-aps/datafordeler.git'
     cd datafordeler
     git checkout feature/19886_documentation
     git submodule update --init --recursive
     
Or (using a modern git implementation):

     git clone --recursive -j8 -b feature/19886_documentation git@github.com:magenta-aps/datafordeler.git
