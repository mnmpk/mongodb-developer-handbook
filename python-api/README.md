# Initial setup
## Setup virtual environments
```
python3 -m venv env-pymongo-sample
source env-pymongo-sample/bin/activate
```

## Install dependencies
```
python -m pip install 'fastapi[all]' 'pymongo[srv]' python-dotenv
```

## Run the app
```
cd src
python -m uvicorn main:app --reload
```