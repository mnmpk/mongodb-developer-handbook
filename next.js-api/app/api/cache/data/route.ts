import { connect } from "@/app/lib/database";
import { NextRequest, NextResponse } from "next/server";

export async function GET(
    req: NextRequest,
    res: NextResponse
) {
    const coll = (await connect()).collection('cacheObject');
    const size = req?.nextUrl?.searchParams.get('size');
    if (size) {
        const obj = { value: generateRandomString(parseInt(size.toString())) };
        await coll.insertOne(obj);
        return NextResponse.json(obj);
    }
}
function generateRandomString(length: number = 1024) {
    let result = '';
    const characters = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789';
    const charactersLength = characters.length;
    for (let i = 0; i < length; i++) {
        result += characters.charAt(Math.floor(Math.random() * charactersLength));
    }
    return result;
}