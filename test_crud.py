"""Integration test against a real Java server with a temporary CSV file.
Run after compilation: python3 test_crud.py
"""
import pathlib
import subprocess
import tempfile
import time
import urllib.error
import urllib.parse
import urllib.request

ROOT = pathlib.Path(__file__).resolve().parent
BASE = 'http://127.0.0.1:8088'


def request(path='/', data=None, headers=None):
    body = None if data is None else urllib.parse.urlencode(data).encode()
    req = urllib.request.Request(BASE + path, data=body, headers=headers or {})
    try:
        response = urllib.request.urlopen(req)
    except urllib.error.HTTPError as error:
        response = error
    with response:
        return response.status, response.read().decode()


def start(file):
    process = subprocess.Popen(['java', '-Dport=8088', '-Ddata.file=' + str(file),
                                '-cp', 'out', 'course.crud.Application'], cwd=ROOT,
                               stdout=subprocess.DEVNULL, stderr=subprocess.PIPE)
    for _ in range(100):
        if process.poll() is not None:
            raise RuntimeError(process.stderr.read().decode())
        try:
            if request()[0] == 200:
                return process
        except OSError:
            pass
        time.sleep(0.05)
    process.terminate()
    raise RuntimeError('Server did not start')


def stop(process):
    process.terminate()
    process.wait(timeout=5)
    process.stderr.close()


with tempfile.TemporaryDirectory() as directory:
    file = pathlib.Path(directory) / 'customers.csv'
    server = start(file)
    try:
        assert 'Alex Green' in request()[1]
        data = dict(id=0, firstName='Zoë', lastName='O\'Neil, "West"', email='zoe@example.com')
        assert request('/save', data)[0] == 200
        assert 'Zoë' in request('/?q=zo')[1]
        assert 'Customer added.' in request('/save', dict(id=0, firstName='<b>Test</b>',
                    lastName='Name', email='test@example.com'))[1]
        html = request()[1]
        assert '&lt;b&gt;Test&lt;/b&gt;' in html and '<b>Test</b>' not in html
        data.update(id=3, firstName='Zoe', email='updated@example.com')
        assert 'Customer updated.' in request('/save', data)[1]
        assert 'updated@example.com' in request('/?edit=3')[1]
        assert request('/save', dict(id=0, firstName='', lastName='Name', email='bad'))[0] == 400
        assert request('/delete', dict(id=999))[0] == 404
        assert request('/?edit=999')[0] == 404
        assert request('/save', data, {'Origin': 'https://unrelated.example'})[0] == 403
        assert 'Customer deleted.' in request('/delete', dict(id=4))[1]
        assert 'Test&lt;' not in request()[1]
    finally:
        stop(server)
    server = start(file)
    try:
        html = request()[1]
        assert 'updated@example.com' in html and 'O&#39;Neil, &quot;West&quot;' in html
        assert request('/delete', dict(id=3))[0] == 200
        assert 'updated@example.com' not in request()[1]
    finally:
        stop(server)
print('PASS: create, list, update, search, delete, validation, HTML escaping and persistence.')
