from contextlib import asynccontextmanager
from multiprocessing import Process
from fastapi import FastAPI
from dotenv import dotenv_values
from pymongo import MongoClient
from routes import router as book_router

config = dotenv_values(".env")


@asynccontextmanager
async def lifespan(app: FastAPI):
    app.mongodb_client = MongoClient(config["ATLAS_URI"])
    app.database = app.mongodb_client[config["DB_NAME"]]
    print("Connected to the MongoDB database!")
    p = Process(target=watch, args=())
    p.start()
    yield
    p.join()
    app.mongodb_client.close()

app = FastAPI(lifespan=lifespan)

@app.get("/")
async def root():
    return {"message": "Welcome to the PyMongo tutorial!"}


app.include_router(book_router, tags=["books"], prefix="/book")

def watch():
    change_stream = MongoClient(config["ATLAS_URI"])[config["DB_NAME"]].books.watch()
    for change in change_stream:
        print(change)
        print('') # for readability only
    