import axios from "axios";
import { getCsrfToken } from '../csrf';

const API_URL = "/api/contacts";

const UUID_PATTERN = /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i;

function validateContactId(id) {
    if (typeof id !== 'string' || !UUID_PATTERN.test(id)) {
        throw new Error('Invalid contact id');
    }
    return id;
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
    const contactId = validateContactId(id);
    return await axios.get(`${API_URL}/${encodeURIComponent(contactId)}`, { withCredentials: true });
}

export async function updateContact(id, contact) {
    const contactId = validateContactId(id);
    return await axios.put(`${API_URL}/${encodeURIComponent(contactId)}`, contact, {
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
    const contactId = validateContactId(id);
    return await axios.delete(`${API_URL}/${encodeURIComponent(contactId)}`, {
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