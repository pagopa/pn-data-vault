./deploy.sh -p profilo_core -r eu-west-3 -s1 subnet-014fecaeb8265b5c0 -s2 subnet-00c9b46dbd1ccaae2 -i vpc-0737683ae260d7814 -c 10.0.0.0/16 \
            -a arn:aws:acm:eu-west-3:804103868123:certificate/254d67b4-868e-42ae-9060-dd0e16b576e5 -z Z02950753TGM2501R0L7F -d api.confidential-test.it \
            -nv vpc-0f4051976b166508f -ns1 subnet-0d885ca69c9b142e2 -ns2 subnet-0fcf9f9adfd6b299e
