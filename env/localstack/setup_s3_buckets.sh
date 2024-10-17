#!/bin/sh
echo "Creating 'fine-car-images' bucket"
env AWS_ACCESS_KEY_ID=dev AWS_SECRET_ACCESS_KEY=dev aws --endpoint-url=http://localhost:4566 s3 mb s3://fine-car-images

env AWS_ACCESS_KEY_ID=dev AWS_SECRET_ACCESS_KEY=dev aws --endpoint-url=http://localhost:4566 s3 cp /var/data/img/AB2565EP_570080547.jpg s3://fine-car-images/AB2565EP_570080547.jpg
env AWS_ACCESS_KEY_ID=dev AWS_SECRET_ACCESS_KEY=dev aws --endpoint-url=http://localhost:4566 s3 cp /var/data/img/AC7136CM_572352720.jpg s3://fine-car-images/AC7136CM_572352720.jpg
env AWS_ACCESS_KEY_ID=dev AWS_SECRET_ACCESS_KEY=dev aws --endpoint-url=http://localhost:4566 s3 cp /var/data/img/AI0385EA_572342014.jpg s3://fine-car-images/AI0385EA_572342014.jpg
env AWS_ACCESS_KEY_ID=dev AWS_SECRET_ACCESS_KEY=dev aws --endpoint-url=http://localhost:4566 s3 cp /var/data/img/AX2594BM_572339085.jpg s3://fine-car-images/AX2594BM_572339085.jpg
env AWS_ACCESS_KEY_ID=dev AWS_SECRET_ACCESS_KEY=dev aws --endpoint-url=http://localhost:4566 s3 cp /var/data/img/BC4631PE_572320190.jpg s3://fine-car-images/BC4631PE_572320190.jpg
env AWS_ACCESS_KEY_ID=dev AWS_SECRET_ACCESS_KEY=dev aws --endpoint-url=http://localhost:4566 s3 cp /var/data/img/BC6727OE_565621483.jpg s3://fine-car-images/BC6727OE_565621483.jpg
env AWS_ACCESS_KEY_ID=dev AWS_SECRET_ACCESS_KEY=dev aws --endpoint-url=http://localhost:4566 s3 cp /var/data/img/BH2393TH_572350178.jpg s3://fine-car-images/BH2393TH_572350178.jpg
env AWS_ACCESS_KEY_ID=dev AWS_SECRET_ACCESS_KEY=dev aws --endpoint-url=http://localhost:4566 s3 cp /var/data/img/BX0251EK_567828344.jpg s3://fine-car-images/BX0251EK_567828344.jpg
env AWS_ACCESS_KEY_ID=dev AWS_SECRET_ACCESS_KEY=dev aws --endpoint-url=http://localhost:4566 s3 cp /var/data/img/DI6555IT_572352800.jpg s3://fine-car-images/DI6555IT_572352800.jpg
env AWS_ACCESS_KEY_ID=dev AWS_SECRET_ACCESS_KEY=dev aws --endpoint-url=http://localhost:4566 s3 cp /var/data/img/KA2832BT_572354386.jpg s3://fine-car-images/KA2832BT_572354386.jpg

echo "Ready to accept connections"
touch /var/data/script_complete.flag
