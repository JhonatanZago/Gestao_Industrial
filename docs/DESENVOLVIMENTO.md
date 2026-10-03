# Desenvolvimento

O código segue a separação resource → service → repository, DTOs validados e migrations Flyway. Testes usam JUnit 5, Mockito e MockMvc; a suíte deve permanecer independente de internet e APIs pagas.

Comandos padrão:

~~~powershell
.\gradlew.bat clean test
.\gradlew.bat build
openspec validate homologacao-backend-v1 --type change --strict
~~~

Antes de alterar contrato, atualize testes, documentação e a change OpenSpec correspondente. Não edite migrations históricas e não registre tokens, senhas, prompts ou documentos em logs.
