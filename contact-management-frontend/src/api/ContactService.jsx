import axios from "axios";
import { getCsrfToken } from '../csrf';

const API_URL = "/api/contacts";

const UUID_PATTERN = /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i;

let idCounter = 0;

export const makeId = () => (typeof crypto !== 'undefined' && crypto.randomUUID
    ? crypto.randomUUID()
    : `row-${Date.now()}-${++idCounter}`);

export const makeEmptyEmailRow = () => ({ label: '', email: '', _key: makeId() });
export const makeEmptyPhoneRow = () => ({ label: '', phone: '', _key: makeId() });

export function updateRowAt(rows, index, field, value) {
    const updated = [...rows];
    updated[index] = { ...updated[index], [field]: value };
    return updated;
}

export function appendRow(rows, makeRow) {
    return [...rows, makeRow()];
}

export function removeRowAt(rows, index, makeRow) {
    const updated = rows.filter((_, i) => i !== index);
    return updated.length > 0 ? updated : [makeRow()];
}

function validateContactId(id) {
    if (typeof id !== 'string' || !UUID_PATTERN.test(id)) {
        throw new Error('Invalid contact id');
    }
    return id;
}

function buildContactResourceUrl(validatedId) {
    return `${API_URL}/${encodeURIComponent(validatedId)}`;
}

export async function saveContact(contact) {
    return await axios.post(API_URL, contact, {
        withCredentials: true,
        headers: { 'X-XSRF-TOKEN': getCsrfToken() }
    });
}

export async function getContacts(page = 0, size = 10, search = '') {
    const query = search ? `&search=${encodeURIComponent(search)}` : '';
    return await axios.get(`${API_URL}?page=${page}&size=${size}${query}`, { withCredentials: true });
}

export async function getContact(id) {
    const validatedId = validateContactId(id);
    return await axios.get(buildContactResourceUrl(validatedId), { withCredentials: true });
}

export async function updateContact(id, contact) {
    const validatedId = validateContactId(id);
    return await axios.put(buildContactResourceUrl(validatedId), contact, {
        withCredentials: true,
        headers: { 'X-XSRF-TOKEN': getCsrfToken() }
    });
}

export async function updatePhoto(formData) {
    return await axios.put(`${API_URL}/photo`, formData, {
        withCredentials: true,
        headers: { 'X-XSRF-TOKEN': getCsrfToken() }
    });
}

export async function deleteContact(id) {
    const validatedId = validateContactId(id);
    return await axios.delete(buildContactResourceUrl(validatedId), {
        withCredentials: true,
        headers: { 'X-XSRF-TOKEN': getCsrfToken() }
    });
}

export async function getProfile() {
    return await axios.get('/api/me', { withCredentials: true });
}

export async function changePassword(currentPassword, newPassword) {
    return await axios.put('/api/change-password', { currentPassword, newPassword }, {
        withCredentials: true,
        headers: { 'X-XSRF-TOKEN': getCsrfToken() }
    });
}