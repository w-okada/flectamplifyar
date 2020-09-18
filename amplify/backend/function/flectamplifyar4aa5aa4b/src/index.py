import json
import subprocess
from subprocess import PIPE
import boto3
import os
import hashlib
import time

s3 = boto3.resource('s3')
dynamodb = boto3.resource('dynamodb')


def generate_hash(file):
  with open(file, mode='rb') as f:
    content = f.read()
  hasher = hashlib.sha1()
  hasher.update(content)
  sha1_hash = hasher.hexdigest()
  return sha1_hash

def calc_score(file):
  proc = subprocess.run(f"/opt/arcoreimg eval-img --input_image_path={file}", shell=True, stdout=PIPE, stderr=PIPE, text=True)
  res = proc.stdout
  score = res.rstrip('\n')
  return score

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
    key    = body["key"]
    name   = body["name"]
    

    # ファイルのダウンロード
    src_file = f'public/{key}'
    dst_file = f'/tmp/{os.path.basename(key)}'
    print(f'download from {src_file} to {dst_file}')
    s3.Bucket(bucket).download_file(src_file, dst_file)

    # ハッシュ値計算
    sha1_hash = generate_hash(dst_file)
    
    # Score計算
    score = calc_score(dst_file)
    if score.isdecimal():
      score = int(score)
    print(f'STDOUT: {score}')

    # fileアップロード
    new_file = f'marker/{sha1_hash}.jpg'
    new_key = f'public/{new_file}'
    s3.Bucket(bucket).upload_file(dst_file, new_key)

    # DynamoDB登録
    createAt = int(time.time())
    item = {
      'id'       : sha1_hash,
      'name'     : name,
      'path'     : new_file,
      'score'    : score,
      'owner'    : "tbd",
      'createdAt' : createAt,
      'updatedAt' : createAt,
    }

    marker_table_name = os.environ['API_FLECTAMPLIFYARGRAPH_MARKERTABLE_NAME']    
    print(f'tablename: {marker_table_name}')
    marker_table = dynamodb.Table(marker_table_name)

    # response = marker_table.scan()
    # print(f'response: {response}')
    marker_table.put_item(Item=item)

    return {
      'statusCode': 200,
      'body': json.dumps(item)
    }
  else:
    dict = {"name": "tarou", "age": 23, "gender": "man"}
    return {
      'statusCode': 200,
      'body': json.dumps(dict)
    }


