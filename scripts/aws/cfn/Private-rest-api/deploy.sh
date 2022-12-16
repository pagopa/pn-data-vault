#!/bin/bash

set -Eeuo pipefail
trap cleanup SIGINT SIGTERM ERR EXIT

cleanup() {
  trap - SIGINT SIGTERM ERR EXIT
  # script cleanup here
}

usage() {
      cat <<EOF
    Usage: $(basename "${BASH_SOURCE[0]}")  [-h] [-v] [-p <aws-profile>] -r <aws-region> [-s <subnet-ids>] -i <vpc-id> -c <vpc-cidr-range> -a <certificate-arn -z <private-hosted-zone> 
    
    [-h]                           : this help message
    [-v]                           : verbose mode
    [-p <aws-profile>]             : aws cli profile (optional)
    -r <aws-region>                : aws region as eu-south-1
    [-s1 <subnet-id-1>]            : comma delimited list of subnets
    [-s2 <subnet-id-2>]            : comma delimited list of subnets
    [-i <vpc-id>]                  : VPC Id
    [-c <vpc-cidr-range>]          : VPC CIDR range
    [-a <certificate-arn>]         : Certificate ARN
    -z <private-hosted-zone>       : Private hosted zone Id
    -d <custom-domain-name>        : Custom domain name
    -nv <nlb-vpc-id>               : VPC Id of the NLB
    -ns1 <nlb-subnet-id-1>         : NLB Subnet Id 1
    -ns2 <nlb-subnet-id-2>         : NLB Subnet Id 2
EOF
  exit 1
}
parse_params() {
  while :; do
    case "${1-}" in
    -h | --help) usage ;;
    -v | --verbose) set -x ;;
    -p | --profile) 
      API_PROFILE="${2-}"
      shift
      ;;
    -r | --region) 
      REGION="${2-}"
      shift
      ;;
    -s1 | --subnet-id-1) 
      SUBNETS_ID_1="${2-}"
      shift
      ;;
    -s2 | --subnet-id-2) 
      SUBNETS_ID_2="${2-}"
      shift
      ;;
    -i | --vpc-id) 
      VPC_ID="${2-}"
      shift
      ;;
    -c | --vpc-cidr-range) 
      VPC_CIDR_RANGE="${2-}"
      shift
      ;;
    -a | --certificate-arn) 
      CERTIFICATE_ARN="${2-}"
      shift 
      ;;
    -z | --private-hosted-zone) 
      PRIVATE_HOSTED_ZONE_ID="${2-}"
      shift
      ;;
    -d | --custom-domain-name) 
      CUSTOM_DOMAIN_NAME="${2-}"
      shift
      ;;
    -nv | --nlb-vpc-id) 
      NLB_VPC_ID="${2-}"
      shift
      ;;
    -ns1 | --nlb-subnet-id-1) 
      NLB_SUBNET_1="${2-}"
      shift
      ;;
    -ns2 | --nlb-subnet-id-2) 
      NLB_SUBNET_2="${2-}"
      shift
      ;;
    -?*) die "Unknown option: $1" ;;
    *) break ;;
    esac
    shift
  done

  args=("$@")
  return 0
}

dump_params(){
    echo ""
    echo "######     PARAMETERS     ######"
    echo "################################"
    echo "SUBNETS_1 = ${SUBNETS_ID_1}"
    echo "SUBNETS_2 = ${SUBNETS_ID_2}"
    echo "VPC_ID = ${VPC_ID}"
    echo "VPC_CIDR_BLOCK = ${VPC_CIDR_RANGE}"
    echo "API_PROFILE = ${API_PROFILE}"
    echo "REGION = ${REGION}"
    echo "CERTIFICATE_ARN = ${CERTIFICATE_ARN}"
    echo "CUSTOM_DOMAIN_NAME = ${CUSTOM_DOMAIN_NAME}"
    echo "PRIVATE_HOSTED_ZONE_ID = ${PRIVATE_HOSTED_ZONE_ID}"
    echo "NLB_SUBNET_1 = ${NLB_SUBNET_1}"
    echo "NLB_SUBNET_2 = ${NLB_SUBNET_2}"
    echo "NLB_VPC_ID = ${NLB_VPC_ID}"
}

# START SCRIPT

parse_params "$@"
dump_params

#This functions deploys a private API gateway and VPC Endpoint
function deployAPI() {
    echo "Deploying Private REST API with VPC Endpoint."

    SUBNETS_ID="$SUBNETS_ID_1,$SUBNETS_ID_2" #NLB Allows for 2 targets

    aws cloudformation deploy --template-file ./confidential-information-vpc-endpoint.yaml \
    --stack-name Confidential-Information-VPC-Endpoint \
    --capabilities CAPABILITY_IAM \
    --parameter-overrides \
        SubnetIds=$SUBNETS_ID \
        VpcId=$VPC_ID \
        VpcCidrBlock=$VPC_CIDR_RANGE \
    --profile $API_PROFILE \
    --region $REGION
}

deployAPI

#This function gets the outputs of the REST API Id
function getRestApiId() {
    REST_API_ID=$( aws cloudformation describe-stacks --profile $API_PROFILE --region $REGION --stack-name 'Confidential-Information-VPC-Endpoint' --query "Stacks[*].Outputs[?OutputKey=='RestApiId'].OutputValue" --output text )
    echo $REST_API_ID
}

#This function returns the private IP address of the network interface
function getVPCEndpointPrivateIP() {
    VpcEndpointId=$( aws cloudformation describe-stacks --profile $API_PROFILE --region $REGION --stack-name 'Confidential-Information-VPC-Endpoint' --query "Stacks[*].Outputs[?OutputKey=='VpcEndpointId'].OutputValue" --output text )

    NetworkInterfaceIds=$( aws ec2 describe-vpc-endpoints --profile $API_PROFILE --region $REGION --vpc-endpoint-ids "${VpcEndpointId}" --query "VpcEndpoints[*].NetworkInterfaceIds[]" --output text )
    for networkId in $NetworkInterfaceIds
    do
        ipArray=$( aws ec2 describe-network-interfaces --profile $API_PROFILE --region $REGION --filters Name=network-interface-id,Values="$networkId" --query "NetworkInterfaces[*].PrivateIpAddresses[*].PrivateIpAddress[]" --output text )
        arrays+=($ipArray)
    done
    return_array=${arrays[0]},${arrays[1]}
    echo $return_array
}

#This functions deploys an internal Network load balancer, to create 
#custom domain names for the rest API
function deployNLB() {
    echo "Deploying Custom Domain resources."

    VPC_ENDPOINT_IP_ARRAY=$(getVPCEndpointPrivateIP)
    echo $VPC_ENDPOINT_IP_ARRAY
    NLB_SUBNETS="$NLB_SUBNET_1,$NLB_SUBNET_2"
    echo $NLB_SUBNETS
    REST_API_ID=$(getRestApiId)
    echo $REST_API_ID

    aws cloudformation deploy --template-file ./custom-domain-name.yaml \
    --stack-name Custom-domain-name \
    --parameter-overrides \
        CertificateArn=$CERTIFICATE_ARN \
        FQDName=$CUSTOM_DOMAIN_NAME \
        HostedZoneID=$PRIVATE_HOSTED_ZONE_ID \
        NLBSubnets=$NLB_SUBNETS \
        RestApiId=$REST_API_ID \
        VpcId=$NLB_VPC_ID \
        VpcEndpointIPs=$VPC_ENDPOINT_IP_ARRAY \
    --profile $API_PROFILE \
    --region $REGION
}

deployNLB

