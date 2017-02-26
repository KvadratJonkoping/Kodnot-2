# Kodnöt #2

Nu är det dags för Kvadrats kodnöt nummer 2.

Du är med och arrangerar en slalomtävling. Efter att alla åkt sina tävlingåk
kommer tävlingsgeneralen och säger: 
- Då tar du och tar fram resultatlistan i lagtävlingen.
- Ok, hur gör jag det? frågar du.
- Ta de tre snabbaste tiderna för åkare från samma klubb. Minst en tjej och minst
  en kille. Dom tillsammans bildar ett lag. Deras totala åktid blir lagets åktid.
  Lätt som en plätt!

## Uppgiften

Räkna ut resultatet i lagtävlingen utifrån csv filen.

Ett lag definieras som tre åkare från samma klubb där det måste vara antingen
två tjejer och en kille eller två killar och en tjej i laget.

Ett lags tid är summan av lagets deltagares summerade tider.

En klubb kan ha flera lag.

När du har ett resultat, skapa en pull-request till detta repository innehållandes 
en mapp som motsvarar ditt användarnamn här på github. 
I denna mapp skapa en mapp med namnet på det programmeringsspråk du använt (Se strukturen nedan).

I denna katalog ska din källkod finnas och en README.md med instruktioner hur
programmet exekveras. Det ska även finnas en resultatlista där det går att läsa
ut vilka åkare som tillhör vilket lag och vilken klubb laget representerar samt
lagets totala tid.

## Katalogstruktur
    .

    ├── användarnamn                # ditt github username
    │   ├── programmeringsspråk
    │      ├── källkod              # import com.apache...
    │      ├── Resultatlista        # valfritt textformat, csv, json
    │      ├── README.md  
    │             Instruktioner  
    └── ...
