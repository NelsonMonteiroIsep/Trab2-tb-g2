# Trab2-tb-g2

Projeto em Java sdk 21

Para ser possivel dar run é necessário:

📌 Requisitos
Java JDK 21+

IntelliJ IDEA (Community ou Ultimate)

JavaFX SDK 21+


✅ 1. Abrir o projeto
Vai a File > Open... no IntelliJ e seleciona a pasta do projeto.

✅ 2. Adicionar o JavaFX SDK
Vai a File > Project Structure > Libraries

Clica no + e escolhe Java

Seleciona a pasta lib do JavaFX SDK (ex: C:/javafx-sdk-21/lib)

Aplica as alterações

✅ 3. Configurar o Main no IntelliJ
Vai a Run > Edit Configurations

Clica em + > Application

Preenche os campos:

Name: Main

Main class: isep.crescendo.Main

Module: Crescendo.Main

VM options:

--module-path "C:\Program Files\Java\javafx-sdk-21.0.7\lib" --add-modules javafx.controls,javafx.fxml

