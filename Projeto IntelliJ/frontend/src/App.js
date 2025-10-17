import React, { useState, useEffect } from 'react';
import './App.css';

function App() {
    // 'useState' para armazenar a mensagem que virá do backend
    const [message, setMessage] = useState('');

    // 'useEffect' para executar a busca de dados quando o componente carregar
    useEffect(() => {
        // Usamos fetch para chamar nosso endpoint no backend
        fetch('/api/test')
            .then(response => response.json()) // Converte a resposta para JSON
            .then(data => setMessage(data.message)) // Pega a mensagem do JSON e atualiza nosso estado
            .catch(error => console.error('Houve um erro ao buscar os dados!', error)); // Trata possíveis erros
    }, []); // O array vazio [] significa que este efeito roda apenas uma vez

    return (
        <div className="App">
            <header className="App-header">
                <h1>Teste de Conexão Frontend (React) - Backend (Spring)</h1>
                <p>
                    {/* Exibe a mensagem do backend ou um texto de carregamento */}
                    {message || 'Carregando mensagem do backend...'}
                </p>
            </header>
        </div>
    );
}

export default App;