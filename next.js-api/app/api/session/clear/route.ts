import { clearSession, readSession } from "@/app/lib/session";
import { NextRequest, NextResponse } from "next/server";

export async function GET (
    req: NextRequest,
    res: NextResponse
) {
    await clearSession();
    return NextResponse.json(readSession());
}