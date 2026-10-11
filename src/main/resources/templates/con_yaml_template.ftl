serviceAddress: ${r'${SMARTSERVICE_RPC_HOST:127.0.0.1}'}
servicePort: ${r'${SMARTSERVICE_RPC_PORT:20880}'}

registryType: 0
registryAddress: ${r'${SMARTSERVICE_REGISTRY_ADDRESS:127.0.0.1:2181}'}

server:
  port: ${portNum?c}
