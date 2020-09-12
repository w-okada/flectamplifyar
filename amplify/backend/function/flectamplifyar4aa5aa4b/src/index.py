import json
import subprocess
from subprocess import PIPE
import boto3
import os

s3 = boto3.resource('s3')
s3_cli = boto3.client('s3')

def handler(event, context):
  print('received event:')
  print(event)
  print(os.environ)
  
  
  method = event['httpMethod']
  path   = event['path']
  
  if method == 'POST' and path == "/markers":
    body = json.loads(event["body"])
    bucket = body["bucket"]
    region = body["region"]
    key = body["key"]
    
    src_file = f'public/{key}'
    dst_file = f'/tmp/{key}'
    print(f'download from {src_file} to {dst_file}')
    s3.Bucket(bucket).download_file(src_file, dst_file)
    
    proc = subprocess.run("ls /tmp -la", shell=True, stdout=PIPE, stderr=PIPE, text=True)
    res = proc.stdout
    print('STDOUT: {}'.format(res))
    
    proc = subprocess.run(f"/opt/arcoreimg eval-img --input_image_path={dst_file}", shell=True, stdout=PIPE, stderr=PIPE, text=True)
    res = proc.stdout
    print('STDOUT: {}'.format(res))
    dict = {"bucket": bucket, "region": region, "key": key, "score":res.rstrip('\n')}

    return {
      'statusCode': 200,
      'body': json.dumps(dict)
    }
  else:
    dict = {"name": "tarou", "age": 23, "gender": "man"}
    return {
      'statusCode': 200,
      'body': json.dumps(dict)
    }



  # proc = subprocess.run("ls -la", shell=True, stdout=PIPE, stderr=PIPE, text=True)
  # res = proc.stdout
  # print('STDOUT: {}'.format(res))
  
  # proc = subprocess.run("ls /opt", shell=True, stdout=PIPE, stderr=PIPE, text=True)
  # res = proc.stdout
  # print('STDOUT: {}'.format(res))
  
  # proc = subprocess.run("ls /opt/*", shell=True, stdout=PIPE, stderr=PIPE, text=True)
  # res = proc.stdout
  # print('STDOUT: {}'.format(res))
  
  # proc = subprocess.run("ls /opt/*/*", shell=True, stdout=PIPE, stderr=PIPE, text=True)
  # res = proc.stdout
  # print('STDOUT: {}'.format(res))

  # proc = subprocess.run("/opt/arcoreimg", shell=True, stdout=PIPE, stderr=PIPE, text=True)
  # res = proc.stdout
  # err = proc.stderr
  # print('STDOUT: {}'.format(res))
  # print('STDERR: {}'.format(err))



