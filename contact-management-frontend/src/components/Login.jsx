import React, { useState } from 'react'
import {
    Typography,
    Button,
    Box,
    Container,
    Card,
    CardActions,
    CardHeader,
    CardContent,
    TextField
} from '@mui/material';
import { useNavigate, Link } from 'react-router-dom';
import axios from 'axios';
import { useAuth } from './AuthContext';

export default function Login() {
    const [username, setUsername] = useState('');
    const [password, setPassword] = useState('');
    const [error, setError] = useState('');
    const navigate = useNavigate();
    const { login } = useAuth();

    const handleLogin = async (e) => {
        e.preventDefault();
        setError('');

        const loginData = { username, password };

        try {
            const response = await axios.post('http://localhost:8081/login', loginData, {
                withCredentials: true
            });
            if (response.status === 200) {
                login();
                navigate('/loginSuccess');
            }
        } catch (error) {
            setError('Invalid username or password. Please retry!');
        }
    };

    return (
        <Container maxWidth="xs">
            <Card sx={{ mt: 8, borderRadius: 2, boxShadow: 2 }}>
                <CardHeader
                    title="Login"
                    sx={{ textAlign: 'center', pb: 0 }}
                    titleTypographyProps={{ variant: 'h5', fontWeight: 500 }}
                />
                <CardContent>
                    <Box
                        component="form"
                        onSubmit={handleLogin}
                        sx={{ display: 'flex', flexDirection: 'column', gap: 2, mt: 1 }}
                    >
                        {error && (
                            <Typography variant="body2" color="error" align="center">
                                {error}
                            </Typography>
                        )}

                        <TextField
                            label="Username"
                            variant="outlined"
                            size="small"
                            fullWidth
                            value={username}
                            onChange={(e) => setUsername(e.target.value)}
                            required
                        />

                        <TextField
                            label="Password"
                            variant="outlined"
                            size="small"
                            type="password"
                            fullWidth
                            value={password}
                            onChange={(e) => setPassword(e.target.value)}
                            required
                        />

                        <Button type="submit" variant="contained" fullWidth>
                            Login
                        </Button>
                    </Box>
                </CardContent>
                <CardActions sx={{ justifyContent: 'center', pb: 2 }}>
                    <Link to="/registration" style={{ fontSize: '0.9rem' }}>
                        Don't have an account? Register
                    </Link>
                </CardActions>
            </Card>
        </Container>
    );
}