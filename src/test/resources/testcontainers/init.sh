echo " - Create pn-data-vault TABLES"
aws --profile default --region us-east-1 --endpoint-url=http://localstack:4566 \
    dynamodb create-table \
    --table-name ConfidentialObjects \
    --attribute-definitions \
        AttributeName=hashKey,AttributeType=S \
        AttributeName=sortKey,AttributeType=S \
    --key-schema \
        AttributeName=hashKey,KeyType=HASH \
        AttributeName=sortKey,KeyType=SORT \
    --provisioned-throughput \
        ReadCapacityUnits=10,WriteCapacityUnits=5

# array of ConfidentialObjects entity
TABLE_NAME="ConfidentialObjects"
data=(
    '{"hashKey": {"S": "TIMELINE#IUN1"}, "sortKey": {"S": "el1"}, "taxId": {"S": "Descrizione 1"}, "denomination": {"S": "Nome"}}'
    '{"hashKey": {"S": "TIMELINE#IUN1"}, "sortKey": {"S": "el2"}, "taxId": {"S": "Descrizione 1"}, "denomination": {"S": "Nome"}}'
    '{"hashKey": {"S": "TIMELINE#IUN1"}, "sortKey": {"S": "el3"}, "taxId": {"S": "Descrizione 1"}, "denomination": {"S": "Nome"}}'
    )

# Loop for put item into DynamoDB
for item in "${data[@]}"; do
    aws --profile default --region us-east-1 --endpoint-url=http://localstack:4566 \
     dynamodb put-item \
    --table-name "$TABLE_NAME" \
    --item "$item"
done

echo "Initialization terminated"