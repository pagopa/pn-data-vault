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

echo " - Create pn-delivery TABLES"
aws --profile default --region us-east-1 --endpoint-url=http://localstack:4566 \
    dynamodb create-table \
    --table-name Notifications \
    --attribute-definitions \
        AttributeName=iun,AttributeType=S \
    --key-schema \
        AttributeName=iun,KeyType=HASH \
    --provisioned-throughput \
        ReadCapacityUnits=20,WriteCapacityUnits=10 \
    --stream-specification \
        StreamEnabled=true,StreamViewType=NEW_IMAGE


aws --profile default --region us-east-1 --endpoint-url=http://localstack:4566 \
    dynamodb create-table \
    --table-name NotificationsMetadata \
    --attribute-definitions \
        AttributeName=iun_recipientId,AttributeType=S \
        AttributeName=sentAt,AttributeType=S \
        AttributeName=senderId_creationMonth,AttributeType=S \
        AttributeName=senderId_recipientId,AttributeType=S \
        AttributeName=recipientId_creationMonth,AttributeType=S \
    --key-schema \
        AttributeName=iun_recipientId,KeyType=HASH \
        AttributeName=sentAt,KeyType=RANGE \
    --provisioned-throughput \
        ReadCapacityUnits=10,WriteCapacityUnits=5 \
    --global-secondary-indexes \
    "[
        {
            \"IndexName\": \"senderId\",
            \"KeySchema\": [{\"AttributeName\":\"senderId_creationMonth\",\"KeyType\":\"HASH\"},
                            {\"AttributeName\":\"sentAt\",\"KeyType\":\"RANGE\"}],
            \"Projection\":{
                \"ProjectionType\":\"ALL\"
            },
            \"ProvisionedThroughput\": {
                \"ReadCapacityUnits\": 10,
                \"WriteCapacityUnits\": 5
            }
        },
        {
            \"IndexName\": \"senderId_recipientId\",
            \"KeySchema\": [{\"AttributeName\":\"senderId_recipientId\",\"KeyType\":\"HASH\"},
                            {\"AttributeName\":\"sentAt\",\"KeyType\":\"RANGE\"}],
            \"Projection\":{
                \"ProjectionType\":\"ALL\"
            },
            \"ProvisionedThroughput\": {
                \"ReadCapacityUnits\": 10,
                \"WriteCapacityUnits\": 5
            }
        },
        {
            \"IndexName\": \"recipientId\",
            \"KeySchema\": [{\"AttributeName\":\"recipientId_creationMonth\",\"KeyType\":\"HASH\"},
                            {\"AttributeName\":\"sentAt\",\"KeyType\":\"RANGE\"}],
            \"Projection\":{
                \"ProjectionType\":\"ALL\"
            },
            \"ProvisionedThroughput\": {
                \"ReadCapacityUnits\": 10,
                \"WriteCapacityUnits\": 5
            }
        }
    ]"


aws --profile default --region us-east-1 --endpoint-url=http://localstack:4566 \
    dynamodb create-table \
    --table-name NotificationsCost \
    --attribute-definitions \
        AttributeName=creditorTaxId_noticeCode,AttributeType=S \
    --key-schema \
        AttributeName=creditorTaxId_noticeCode,KeyType=HASH \
    --provisioned-throughput \
        ReadCapacityUnits=20,WriteCapacityUnits=10

aws --profile default --region us-east-1 --endpoint-url=http://localstack:4566 \
    dynamodb create-table \
    --table-name NotificationsQR \
    --attribute-definitions \
        AttributeName=aarQRCodeValue,AttributeType=S \
    --key-schema \
        AttributeName=aarQRCodeValue,KeyType=HASH \
    --provisioned-throughput \
        ReadCapacityUnits=20,WriteCapacityUnits=10


echo " - Create pn-mandates TABLES"
aws --profile default --region us-east-1 --endpoint-url=http://localstack:4566 \
    dynamodb create-table \
    --table-name Mandate  \
    --attribute-definitions \
        AttributeName=pk,AttributeType=S \
        AttributeName=sk,AttributeType=S \
        AttributeName=s_delegate,AttributeType=S \
        AttributeName=i_state,AttributeType=N \
    --key-schema \
        AttributeName=pk,KeyType=HASH \
        AttributeName=sk,KeyType=RANGE \
    --provisioned-throughput \
        ReadCapacityUnits=10,WriteCapacityUnits=5 \
    --global-secondary-indexes \
    "[
        {
            \"IndexName\": \"delegate-state-gsi\",
            \"KeySchema\": [{\"AttributeName\":\"s_delegate\",\"KeyType\":\"HASH\"},
                            {\"AttributeName\":\"i_state\",\"KeyType\":\"RANGE\"}],
            \"Projection\":{
                \"ProjectionType\":\"ALL\"
            },
            \"ProvisionedThroughput\": {
                \"ReadCapacityUnits\": 10,
                \"WriteCapacityUnits\": 5
            }
        }
    ]"
  
aws --profile default --region us-east-1 --endpoint-url=http://localstack:4566 \
    dynamodb create-table \
    --table-name MandateHistory  \
    --attribute-definitions \
        AttributeName=pk,AttributeType=S \
        AttributeName=sk,AttributeType=S \
    --key-schema \
        AttributeName=pk,KeyType=HASH \
        AttributeName=sk,KeyType=RANGE \
    --provisioned-throughput \
        ReadCapacityUnits=10,WriteCapacityUnits=5

aws --profile default --region us-east-1 --endpoint-url=http://localstack:4566 \
    dynamodb create-table \
    --table-name OptInSent  \
    --attribute-definitions \
        AttributeName=pk,AttributeType=S \
    --key-schema \
        AttributeName=pk,KeyType=HASH \
    --provisioned-throughput \
        ReadCapacityUnits=10,WriteCapacityUnits=5

aws --profile default --region us-east-1 --endpoint-url=http://localstack:4566 \
    dynamodb create-table \
    --table-name audit_storage  \
    --attribute-definitions \
        AttributeName=type,AttributeType=S \
        AttributeName=logDate,AttributeType=S \
    --key-schema \
        AttributeName=type,KeyType=HASH \
        AttributeName=logDate,KeyType=RANGE \
    --provisioned-throughput \
        ReadCapacityUnits=10,WriteCapacityUnits=5
    
aws --profile default --region us-east-1 --endpoint-url=http://localstack:4566 \
    ssm put-parameter \
    --name "MapPaMVP" \
    --type String \
    --value "[
                 {
                     \"paTaxId\": \"01199250158\",
                     \"isMVP\": true
                 },
                 {
                     \"paTaxId\": \"00215150236\",
                     \"isMVP\": true
                 },
                 {
                     \"paTaxId\": \"00189800204\",
                     \"isMVP\": true
                 },
                 {
                     \"paTaxId\": \"00301970190\",
                     \"isMVP\": true
                 }
             ]"
echo "Initialization terminated"