import 'server-only'
import { SignJWT, jwtVerify } from 'jose'
import { ObjectId, ReturnDocument } from 'mongodb'
import { cookies } from 'next/headers'
import { connect } from './database';
const database = 'mongodb-developer';
const collection = 'sessions';
const ttl = 60 * 60 * 24 * 7; // 7 days in seconds

const secretKey = "SESSION_KEY";//process.env.SESSION_SECRET
const encodedKey = new TextEncoder().encode(secretKey)

async function encrypt(payload: any) {
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
    console.log('Failed to verify session');
    return null;
  }
}

async function readData(key: ObjectId, ttl: number) {
  return (await connect(database)).collection(collection).findOneAndUpdate({ _id: key }, { $set: { expiresAt: new Date(Date.now() + ttl * 1000) } });
}
async function writeData(key: ObjectId, data: any, ttl: number) {
  try {
    return await (await connect(database)).collection(collection).findOneAndReplace({ _id: key }, { _id: key, v: data, expiresAt: new Date(Date.now() + ttl * 1000) }, { returnDocument: ReturnDocument.AFTER, upsert: true });
  } catch (e) {
    console.error(`Failed to cache data for key=${key}`, e);
  }
}

async function getCookie() {
  const cookieStore = await cookies();
  let sessionJWT = cookieStore.get('session')?.value;
  return await decrypt(sessionJWT);
}
async function touchCookie(data: any) {
  const cookieStore = await cookies();
  const jwt = await encrypt(data);
  if (jwt)
    cookieStore.set('session', jwt, {
      httpOnly: true,
      secure: true,
      expires: new Date(Date.now() + ttl * 1000),
      sameSite: 'lax',
      path: '/',
    });
}

async function newSession(data?: any) {
  return await writeData(new ObjectId(), data || {}, ttl);
}
export async function readSession() {
  const session = await getCookie();
  let ret = null;
  if (session)
    ret = await readData(new ObjectId(session._id as string), ttl);
  else
    ret = await newSession();
  touchCookie(ret);
  return ret;
}
export async function putSession(data: any) {
  const session = await getCookie();
  let ret = null;
  if (session)
    ret = await writeData(new ObjectId(session._id as string), data, ttl);
  else
    ret = await newSession(data);
  touchCookie(ret);
  return ret;
}
export async function clearSession() {
  await putSession({});
}
export async function destory() {
  const cookieStore = await cookies();
  let sessionJWT = cookieStore.get('session')?.value;
  if (sessionJWT) {
    const session: any = await decrypt(sessionJWT);
    await (await connect(database)).collection(collection).deleteOne({ _id: new ObjectId(session._id as string) });
    cookieStore.delete('session');
  }
}
