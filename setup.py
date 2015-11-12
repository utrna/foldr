#!/usr/bin/env python
# -*- coding: utf-8 -*-
# @author <ashtoncberger@utexas.edu>
# ------------------------------------------------
import os
try:
    from setuptools import setup, Command, Extension
except ImportError:
    from distutils.core import setup, Command, Extension

#cleanup 
class CleanCommand(Command):
    '''Custom clean command to tidy up the project root.'''
    user_options = []
    def initialize_options(self):
        pass
    def finalize_options(self):
        pass
    def run(self):
        os.system('rm -vrf ./build ./dist ./*.pyc ./*.tgz ./*.egg-info')

#get readme info
with open('README.md') as readme_file:
    readme = readme_file.read()

#define requirements
requirements = []

#setup options
setup(
    name='foldrr',
    version='0.0.0',
    description="Ribosomal RNA secondary structure prediction software.",
    long_description=readme,
    author='Ashton Berger',
    author_email='ashtoncberger@utexas.edu',
    url='https://ashtoncb@bitbucket.org/ashtoncb/foldrr.git',
    packages=['src'],
    include_package_data=True,
    license="BSD", #is this right?? maybe Academic Free License?
    cmdclass={'clean': CleanCommand,}
    )
