import { putSession, readSession } from "@/app/lib/session";
import { MongoClient } from "mongodb";
const uri = "mongodb+srv://admin:admin12345@demo.uskpz.mongodb.net/mongodb-developer";
const client = new MongoClient(uri);

export async function GET(request: Request) {
    let session: any = readSession();
    console.log(await client.db().collection("sessions").find());
    console.log(await session);
    putSession({ userId: '123' });
    // For example, fetch data from your DB here
    const users = [
      { id: 1, name: 'Alice' },
      { id: 2, name: 'Bob' }
    ];
    return new Response(JSON.stringify(users), {
      status: 200,
      headers: { 'Content-Type': 'application/json' }
    });
  }
   
  export async function POST(request: Request) {
    // Parse the request body
    const body = await request.json();
    const { name } = body;
   
    // e.g. Insert new user into your DB
    const newUser = { id: Date.now(), name };
   
    return new Response(JSON.stringify(newUser), {
      status: 201,
      headers: { 'Content-Type': 'application/json' }
    });
  }