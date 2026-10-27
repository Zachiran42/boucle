# Boucle

[Télécharger Boucle.apk](https://github.com/Zachiran42/boucle/releases/latest/download/Boucle-1.0.0.apk) · [Toutes les versions](https://github.com/Zachiran42/boucle/releases)

Un petit jeu Android de dessin et de réflexes, créé pour Cercle. Trace une boucle, capture les points et garde les obstacles corail à l’extérieur.

- Parties de **60 secondes**, avec **3 vies**.
- Plus une boucle capture de points, plus elle rapporte. Enchaîne les captures pour faire monter le combo.
- Jeu hors ligne, sans compte, publicité ni permission Android.
- Record sauvegardé sur l’appareil et pause pendant la partie.
- Android **8.0 ou version ultérieure**. APK universel, sans service externe.

## Jouer

1. Lance une partie, puis pose un doigt dans la zone de jeu.
2. Trace une boucle autour des points clairs et reviens au point de départ pour la refermer.
3. Évite les obstacles mobiles : toucher un obstacle avec le trait ou en enfermer un coûte une vie.
4. Utilise la pause pour interrompre la partie. Essaie de battre ton record avant la fin du temps.

Le record reste local. Il disparaît si les données de l’application sont effacées ou si celle-ci est désinstallée.

## Installer et ajouter dans Cercle

Télécharge l’APK signé depuis les **Releases** de ce dépôt, puis ouvre-le sur un téléphone Android et suis la confirmation d’installation.

Dans [Cercle](https://github.com/Zachiran42/cercle-store), utilise **Ajouter depuis GitHub**, colle le lien de ce dépôt puis sélectionne l’APK de la publication. Cercle vérifie son identité avant de proposer l’installation. L’ajout personnel reste sur ce téléphone. Pour une présence dans le catalogue partagé, le propriétaire du store importe l’APK dans son administration locale puis republie le catalogue.

Identifiant de l’application : `fr.cercle.boucle`. Première version : `1.0.0`, `versionCode` : `1`. Les prochaines versions doivent conserver cet identifiant et la même clé de signature, avec un `versionCode` supérieur.

## Compiler sous Windows

La compilation utilise directement les outils Android et Java, sans Gradle ni dépendance à télécharger :

- Java JDK 21 ;
- Android SDK Platform 36 ;
- Android SDK Build Tools 36.0.0 ;
- PowerShell 5.1 ou version ultérieure.

Copie `.toolchain.example.json` en `.toolchain.json`, puis renseigne les chemins absolus vers le JDK, `android.jar` et les Build Tools. Tu peux également fournir une configuration externe avec `-ToolchainFile`.

Fournis une clé de signature conservée **en dehors du dépôt**. Le mot de passe est lu depuis la variable d’environnement `BOUCLE_KEYSTORE_PASSWORD` ; le script ne le sauvegarde pas et ne crée pas de clé. La clé et son mot de passe doivent être conservés pour pouvoir publier des mises à jour installables par-dessus la première version.

```powershell
.\scripts\test.ps1
.\scripts\build.ps1 -KeyStore 'C:\cles-privees\boucle.jks' -KeyAlias boucle
```

Le mot de passe de la clé et celui du magasin doivent correspondre pour ce script. Le fichier produit par défaut est `build/Boucle-1.0.0.apk`. Pour choisir un dossier de travail ou une destination :

```powershell
.\scripts\build.ps1 -ToolchainFile 'C:\outils\android.json' -KeyStore 'C:\cles-privees\boucle.jks' -OutputFile 'C:\livraisons\Boucle-1.0.0.apk' -BuildDir 'C:\temp\boucle-build'
```

Le script compile les ressources et les sources Java, produit les fichiers DEX, aligne l’APK, le signe puis vérifie sa signature et son alignement. Il affiche également son SHA-256. Aucun fichier privé de signature ne doit être ajouté à GitHub ou à une archive de sources.

## Structure

- `android/src/fr/cercle/boucle/` : interface native et moteur du jeu.
- `android/res/` : thème et icône vectorielle.
- `tests/GameEngineTest.java` : tests du moteur, exécutés sans Android.
- `scripts/build.ps1` : création de l’APK signé.
- `scripts/test.ps1` : compilation et exécution des tests du moteur.

Les tests du moteur et la compilation ne remplacent pas une validation de l’affichage, des gestes et des performances sur téléphone.

## Licence

Code et visuels sous licence [MIT](LICENSE).