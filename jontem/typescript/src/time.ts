import { Participant } from "./types";

export function sumTime(teamMembers: Array<Participant>) {
    return teamMembers.reduce((soFar, current) => (soFar + current.time), 0);
}

export function formatTime(numerical: number): string {
    const minutes = Math.floor(numerical / 60);
    const remaining = parseInt(Math.round(numerical % 60 * 100).toString()) / 100; // Round remaining part with 2 decimals. The JavaScript way :)

    return `${minutes}min ${remaining}sec`;
}