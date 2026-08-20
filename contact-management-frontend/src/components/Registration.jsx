import axios from 'axios'
import React, { useState } from 'react'
import { useNavigate, Link } from 'react-router-dom'
import { getCsrfToken } from '../csrf';
import { toastSuccess } from '../api/ToastService';

import {
    Card,
    CardHeader,
    CardContent,
    CardActions,
    Container,
    Box,
    Typography,
    TextField,
    Button
} from '@mui/material'

export default function Registration() {

    const [formData, setFormData] = useState({
        firstname: '',
        lastname: '',
        username: '',
        password: '',
        confirmPassword: ''
    })

    const [error, setError] = useState('')
    const navigate = useNavigate()

    const handleChange = (e) => {
        setFormData({
            ...formData,
            [e.target.name]: e.target.value
        });
    };

    const handleSubmit = async (e) => {
        e.preventDefault();

        if (formData.password !== formData.confirmPassword) {
            setError('Passwords do not match');
            return;
        }
        setError('')

        try {
            const { confirmPassword, ...payload } = formData;

            const response = await axios.post('/api/register', payload, {
                withCredentials: true,
                headers: { 'X-XSRF-TOKEN': getCsrfToken() }
            });

            if (response.status === 200 || response.status === 201) {
                toastSuccess('Registration successful! Please log in.');
                navigate('/login');
            } else {
                setError('Registration did not complete. Please try again.');
            }
        }
        catch (err) {
            console.log('Registration failed with status:', err.response?.status);
            setError('An error occurred during user registration');
        }
    }

    return (
        <Container maxWidth="xs">
            <Card sx={{ mt: 6, mb: 4, borderRadius: 2, boxShadow: 2 }}>
                <CardHeader
                    title="Registration"
                    sx={{ textAlign: 'center', pb: 0 }}
                    slotProps={{ title: { variant: 'h5', fontWeight: 500 } }}
                />
                <CardContent>
                    <Box
                        component="form"
                        onSubmit={handleSubmit}
                        sx={{ display: 'flex', flexDirection: 'column', gap: 2, mt: 1 }}
                    >
                        {error && (
                            <Typography variant="body2" color="error" align="center">
                                {error}
                            </Typography>
                        )}

                        <TextField
                            label="First Name"
                            name="firstname"
                            size="small"
                            fullWidth
                            value={formData.firstname}
                            onChange={handleChange}
                            required
                        />

                        <TextField
                            label="Last Name"
                            name="lastname"
                            size="small"
                            fullWidth
                            value={formData.lastname}
                            onChange={handleChange}
                            required
                        />

                        <TextField
                            label="Username"
                            name="username"
                            size="small"
                            fullWidth
                            value={formData.username}
                            onChange={handleChange}
                            required
                        />

                        <TextField
                            label="Password"
                            name="password"
                            type="password"
                            size="small"
                            fullWidth
                            value={formData.password}
                            onChange={handleChange}
                            required
                        />

                        <TextField
                            label="Confirm Password"
                            name="confirmPassword"
                            type="password"
                            size="small"
                            fullWidth
                            value={formData.confirmPassword}
                            onChange={handleChange}
                            required
                        />

                        <Button type="submit" variant="contained" fullWidth>
                            Register
                        </Button>
                    </Box>
                </CardContent>
                <CardActions sx={{ justifyContent: 'center', pb: 2 }}>
                    <Link to="/login" style={{ fontSize: '0.9rem' }}>
                        Already have an account? Login
                    </Link>
                </CardActions>
            </Card>
        </Container>
    );
}