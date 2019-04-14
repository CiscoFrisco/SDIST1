Compilação:

Para compilar o trabalho desenvolvido, tanto em Windows como em Linux, apenas é necessário mudar o diretório atual para a pasta
/src e executar o seguinte comando:
        
	javac *.java

Execução:

Para executar o trabalho são necessários seguir os 3 próximos passos:
 
   
1. Iniciar o Registo RMI:

    Visto que este trabalho implementa a interface RemoteMethodInvocation (RMI) de Java, para o testar corretamente é necessário ainda iniciar o registo RMI com os
    comandos:
    
        - Linux: rmiregistry &
        - Windows: start rmiregistry

    
2. Executar os Peers
    O comando de execução dos \textit{peers} é o seguinte:
       
	java Peer <protocol_version> <peerID> <peer_ap> <MCaddress> <MCport> <MDBaddress> <MDBport> <MDRaddress> <MDRport>
    
        - protocol_version: Versão do protocolo compatível com o peer.
        - peerID: Identificador úncico de um peer.
        - peer_ap: Ponto de acesso do objeto do RMI.
        - MCadress: Endereço IP do canal multicast de controlo.
        - MCport: Porta do canal multicast de controlo.
        - MDBadress: Endereço IP do canal multicast de backup.
        - MDBport: Porta do canal multicast de backup.
        - MDRadress: Endereço IP do canal multicast de restore.
        - MDRport: Porta do canal multicast de restore.
    
     Os endereços IP multicast acima utilizados devem estar entre os valores 224.0.0.0 e 224.0.0.255 que são os endereços multicast reservados para redes locais.
 
    Dentro da pasta /scripts existem ainda 3 scritps (1 de Linux e 2 de Windows) que permitem a execuçãode vários peers de uma só vez. Para isso só é necessário
    executar os comandos:
         
	 - Linux:  ./start.sh <protocol_version> <num_peers> <startID>
         - Windows:
            win_start  <protocol_version> <num_peers> <startID> //or
            win_start_rev <protocol_version> <num_peers> <startID>
        
         - protocol_version: Versão do protocolo compatível com o peer.
         - num_peers: Número de peers a executar.
         - startID: ID a partir do qual vão ser criados novos peers.

        A única diferença entre os dois scripts de Windows é a ordem pela qual os Peers  executam. O script win_start inicia os scripts por ID ascendente enquanto 
	o win_start_rev vai iniciando por ordem descendente.
    

3. Executar a TestApp
    O comando de execução da TestApp é o seguinte:
    
        java TestApp <peer_ap> <operation> <opnd_1> <opnd_2> 
    
        - preer_ap: Ponto de acesso do peer.
        - operation: Protocolo a ser executado.
        - opnd_1: Pode ser o diretório do ficheiro a dar BACKUP/RESTORE/DELETE ou, no caso do protocolo RECLAIM, o espaço máximo de disco (em Kbytes).
        - opnd_2: Utilizado no protocolos BACKUP/BACKUPENH e especifica o replication degree.

    Tal como na execução dos peers também aqui foram desenvolvidos scripts para Windows e Linux que testam: vários protocolos BACKUP concorrentes, vários
    protocolos RESTORE concorrentes e vários protocolos diferentes concorrentes.
  
        - Linux:
        ./multiple_backup.sh [enhancement=true]
        ./multiple_restore.sh [enhancement=true]
        ./multiple_protocols.sh [enhancement=true]

        - Windows:
        backup [enhancement=true]
        restore [enhancement=true]
        protocols [enhancement=true]
   
    Este script tem um argumento opcional que, caso esteja presente,indica que os protocolos devem executar a versão melhorada.


Visto que em alguns pormenores retirámos ideias de artigos e pedaços de código encontrados na internet, enviamos abaixo as funções que as utilizaram com as fontes
respetivas:

Utils.getCharSeparator() -> saber OS - https://stackoverflow.com/questions/228477/how-do-i-programmatically-determine-operating-system-in-java


Utils.bytesToHex(byte[] bytes) -> https://stackoverflow.com/questions/332079/in-java-how-do-i-convert-a-byte-array-to-a-string-of-hex-digits-while-keeping-l


Utils.concatenateArrays(byte[] a, byte[] b) - https://stackoverflow.com/questions/5513152/easy-way-to-concatenate-two-byte-arrays


Utils.getSHA(String input) - https://stackoverflow.com/questions/5531455/how-to-hash-some-string-with-sha256-in-java


Utils.hexStringToByteArray(String s) - https://stackoverflow.com/questions/140131/convert-a-string-representation-of-a-hex-dump-to-a-byte-array-using-java


Utils.getRandomNumber(int low, int high) - https://stackoverflow.com/questions/363681/how-do-i-generate-random-integers-within-a-specific-range-in-java


StoredFile.splitFile() - https://stackoverflow.com/questions/10864317/how-to-break-a-file-into-pieces-using-java


Chunk.serialize() and Chunk.deserialize(String file) - https://www.tutorialspoint.com/java/java_serialization.htm
