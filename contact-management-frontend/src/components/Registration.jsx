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
    Button,
    InputAdornment,
    IconButton
} from '@mui/material'

export default function Registration() {

    const [formData, setFormData] = useState({
        firstname: '',
        lastname: '',
        email: '',
        phone: '',
        password: '',
        confirmPassword: ''
    })

    const [error, setError] = useState('')
    const [showPassword, setShowPassword] = useState(false);
    const [showConfirmPassword, setShowConfirmPassword] = useState(false);
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

        if (!formData.email.trim() && !formData.phone.trim()) {
            setError('Please provide an email address or a phone number');
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
            const message = typeof err.response?.data === 'string'
                ? err.response.data
                : 'An error occurred during user registration';
            setError(message);
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
                            label="Email (optional if phone is provided)"
                            name="email"
                            size="small"
                            fullWidth
                            value={formData.email}
                            onChange={handleChange}
                        />

                        <TextField
                            label="Phone Number (optional if email is provided)"
                            name="phone"
                            size="small"
                            fullWidth
                            value={formData.phone}
                            onChange={handleChange}
                        />

                        <TextField
                            label="Password"
                            name="password"
                            type={showPassword ? 'text' : 'password'}
                            size="small"
                            fullWidth
                            value={formData.password}
                            onChange={handleChange}
                            required
                            slotProps={{
                                input: {
                                    endAdornment: (
                                        <InputAdornment position="end">
                                            <IconButton
                                                onClick={() => setShowPassword(!showPassword)}
                                                edge="end"
                                                size="small"
                                                aria-label={showPassword ? 'Hide password' : 'Show password'}
                                            >
                                                <i className={showPassword ? 'bi bi-eye-slash' : 'bi bi-eye'} aria-hidden="true"></i>
                                            </IconButton>
                                        </InputAdornment>
                                    )
                                }
                            }}
                        />

                        <TextField
                            label="Confirm Password"
                            name="confirmPassword"
                            type={showConfirmPassword ? 'text' : 'password'}
                            size="small"
                            fullWidth
                            value={formData.confirmPassword}
                            onChange={handleChange}
                            required
                            slotProps={{
                                input: {
                                    endAdornment: (
                                        <InputAdornment position="end">
                                            <IconButton
                                                onClick={() => setShowConfirmPassword(!showConfirmPassword)}
                                                edge="end"
                                                size="small"
                                                aria-label={showConfirmPassword ? 'Hide password' : 'Show password'}
                                            >
                                                <i className={showConfirmPassword ? 'bi bi-eye-slash' : 'bi bi-eye'} aria-hidden="true"></i>
                                            </IconButton>
                                        </InputAdornment>
                                    )
                                }
                            }}
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