import json
import subprocess
from subprocess import PIPE

def handler(event, context):
  print('received event:')
  print(event)

  proc = subprocess.run("ls -la", shell=True, stdout=PIPE, stderr=PIPE, text=True)
  res = proc.stdout
  print('STDOUT: {}'.format(res))

  proc = subprocess.run("./arcoreimg", shell=True, stdout=PIPE, stderr=PIPE, text=True)
  res = proc.stdout
  err = proc.stderr
  print('STDOUT: {}'.format(res))
  print('STDERR: {}'.format(err))



  return {
      'statusCode': 200,
      'body': json.dumps(event)
  }
