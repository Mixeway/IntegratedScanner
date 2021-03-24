# Mixeway Testing
Projekt jest agentem systemu Mixeway (https://mixer.corpnet.pl), który jest odpowiedzialny za orkiestracje operacji 
bezpieczeństwa w kontekście realizacji testów bezpieczeństwa.

Agent dystrybuowany jest w formie aplikacji java lub kontenera dockerowego.

## Realizowane testy
* Testy bezpieczeństwa biblitek OpenSource (wspierane JAVA MVN, NPM, Python PIP)
* Testy bezpieczeństwa kodu źródłowego (z wykorzystaniem checkmarx)
* Weryfikacja wycieków haseł (z wykorzystaniem projektu gitleaks)
* Testy kodu IaC (infrastructure as a code z wykorzystaniem tfsec)

## Security Quality Gateway
Po zleceniu testów weryfikowane jest spełnienie polityki bezpieczeństwa. W przypadku gdy polityka jest spełniona, zadanie 
kończy się sukcesem, w przeciwnym przypadku zgłaszany jest błąd 