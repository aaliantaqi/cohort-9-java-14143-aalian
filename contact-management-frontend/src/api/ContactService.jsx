import axios from "axios";
import { getCsrfToken } from '../csrf';

const API_URL = "/api/contacts";

function validateContactId(id) {
    const contactId = Number(id);
    if (!Number.isInteger(contactId) || contactId <= 0) {
        throw new Error('Invalid contact id');
    }
    return contactId;
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
    return await axios.get(`${API_URL}/${contactId}`, { withCredentials: true });
}

export async function updateContact(id, contact) {
    const contactId = validateContactId(id);
    return await axios.put(`${API_URL}/${contactId}`, contact, {
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
    return await axios.delete(`${API_URL}/${contactId}`, {
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