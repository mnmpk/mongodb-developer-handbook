import 'server-only'
import { SignJWT, jwtVerify } from 'jose'
import { ObjectId, ReturnDocument } from 'mongodb'
import { cookies } from 'next/headers'
const { MongoClient } = require("mongodb");

// Replace the uri string with your connection string.
const uri = "mongodb+srv://admin:admin12345@demo.uskpz.mongodb.net/mongodb-developer";
const client = new MongoClient(uri);
const database = 'mongodb-developer';
const collection = 'sessions';
const ttl = 60 * 60 * 24 * 7; // 7 days in seconds

type SessionPayload = {
  _id: string,
  expiresAt: Date,
  v: any
}

const secretKey = "SESSION_KEY";//process.env.SESSION_SECRET
const encodedKey = new TextEncoder().encode(secretKey)

async function encrypt(payload: SessionPayload) {
  if (payload)
    return new SignJWT(payload)
      .setProtectedHeader({ alg: 'HS256' })
      .setIssuedAt()
      .setExpirationTime('7d')
      .sign(encodedKey);
}

async function decrypt(session: string | undefined = '') {
  try {
    const { payload } = await jwtVerify(session, encodedKey, {
      algorithms: ['HS256'],
    })
    return payload;
  } catch (error) {
    console.log('Failed to verify session')
  }
}

async function readData(key: ObjectId, ttl: number) {
  return await client.db(database).collection(collection).findOneAndUpdate({ _id: key }, { $set: { expiresAt: new Date(Date.now() + ttl * 1000) } });
}
async function writeData(key: ObjectId, data: any, ttl: number) {
  try {
    const d = await client.db(database).collection(collection).findOneAndReplace({ _id: key }, { _id: key, v: data, expiresAt: new Date(Date.now() + ttl * 1000) }, { returnDocument: ReturnDocument.AFTER, upsert: true });
    return { ...d, _id: d._id.toString() };
  } catch (e) {
    console.error(`Failed to cache data for key=${key}`, e);
  }
}

export async function createNewSession(data: any) {
  return await encrypt(await writeData(new ObjectId(), data, ttl));
}
export async function readSession() {
  const cookieStore = await cookies();
  let sessionJWT = cookieStore.get('session')?.value;
  const session: any = await decrypt(sessionJWT);

  let ret = null;
  if (session) {
    const s = await readData(new ObjectId(session._id as string), ttl);
    if (s) {
      ret = s;
      sessionJWT = await encrypt(s);
    } else {
      sessionJWT = await createNewSession({});
    }
  } else {
    sessionJWT = await createNewSession({});
  }
  console.log(sessionJWT);
  if (sessionJWT)
    cookieStore.set('session', sessionJWT, {
      httpOnly: true,
      secure: true,
      expires: new Date(Date.now() + ttl * 1000),
      sameSite: 'lax',
      path: '/',
    });
  return ret;
}
export async function putSession(data: any) {
  const cookieStore = await cookies();
  let sessionJWT = cookieStore.get('session')?.value;
  if (sessionJWT) {
    const session: any = await decrypt(sessionJWT);
    if (session) {
      const cachedValue = await readData(new ObjectId(session._id as string), ttl);
      if (cachedValue) {
        sessionJWT = await encrypt(await writeData(new ObjectId(session._id as string), data, ttl));
      } else {
        sessionJWT = await createNewSession(data);
      }
    } else {
      sessionJWT = await createNewSession(data);
    }
  } else {
    sessionJWT = await createNewSession(data);
  }
  if (sessionJWT)
    cookieStore.set('session', sessionJWT, {
      httpOnly: true,
      secure: true,
      expires: new Date(Date.now() + ttl * 1000),
      sameSite: 'lax',
      path: '/',
    });
}
