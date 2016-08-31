from flask import Flask, render_template, request, redirect,send_from_directory

import numpy as np
import pandas as pd
k=pd.DataFrame.from_csv('data.csv')
k1=k.sort_index()





app = Flask(__name__, static_url_path='')

@app.route('/')
def main():
  return redirect('/index')

@app.route('/index')
def index():
  return render_template('index.html')


@app.route('/about')
def about():
  return render_template('about.html')


@app.route('/application')
def application():
  return render_template('services.html')


@app.route('/description',methods=['GET','POST'])
def description():
  return render_template('works.html')



@app.route('/fonts/<fname>')
def fontloader(fname):
    return send_from_directory('fonts',fname)


@app.route('/img/<fname>')
def imgloader(fname):
    return send_from_directory('img',fname)

@app.route('/assets/img/<fname>')
def imgloaderasset(fname):
    return send_from_directory('img',fname)

@app.route('/assets/js/<fname>')
def jsloaderasset(fname):
    return send_from_directory('js',fname)


if __name__ == '__main__':
  app.run(port=33507)



