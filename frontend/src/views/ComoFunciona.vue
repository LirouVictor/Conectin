<template>
    <div class="how-it-works-container">
        <div class="how-it-works-box">
            <h1>Como Funciona o Conectin</h1>
            <p class="intro-text">
                O Conectin é a plataforma ideal para conectar clientes a trabalhadores informais de forma simples e
                segura. Veja como funciona:
            </p>

            <div class="steps-container">
                <div class="step-card" v-for="(step, index) in steps" :key="index">
                    <div class="step-number">{{ step.number }}</div>
                    <h3>{{ step.title }}</h3>
                    <p>{{ step.description }}</p>
                </div>
            </div>

            <div class="benefits-section">
                <h2>Benefícios</h2>
                <div class="benefits-grid">
                    <div class="benefit-card" v-for="(benefit, index) in benefits" :key="index">
                        <i class="benefit-icon">{{ benefit.icon }}</i>
                        <h3>{{ benefit.title }}</h3>
                        <p>{{ benefit.description }}</p>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<script>
export default {
    name: 'ComoFunciona',
    data() {
        return {
            steps: [
                {
                    number: '1',
                    title: 'Escolha a Categoria e Região',
                    description:
                        'Selecione a categoria do serviço (ex.: Pedreiro, Encanador) e informe sua cidade para encontrar trabalhadores próximos.',
                },
                {
                    number: '2',
                    title: 'Escolha um Trabalhador',
                    description:
                        'Veja o ranking dos mais bem avaliados e escolha o trabalhador com base em avaliações e portfólio.',
                },
                {
                    number: '3',
                    title: 'Contratação e Notificação',
                    description:
                        'Envie uma solicitação. O trabalhador será notificado e decidirá se aceita. Após a aceitação, troque contatos via WhatsApp.',
                },
                {
                    number: '4',
                    title: 'Avaliação Após o Serviço',
                    description:
                        'Após o serviço, ambos (cliente e trabalhador) podem avaliar a experiência, com comentários e fotos para referência futura.',
                },
            ],
            benefits: [
                {
                    icon: '✔',
                    title: 'Para Clientes',
                    description: 'Encontre profissionais qualificados e avalie suas experiências.',
                },
                {
                    icon: '✔',
                    title: 'Para Trabalhadores',
                    description: 'Aumente sua visibilidade com avaliações e construa um portfólio.',
                },
                {
                    icon: '✔',
                    title: 'Segurança',
                    description: 'Processo transparente com avaliações mútuas.',
                },
            ],
        };
    },
};
</script>

<style scoped>
.how-it-works-container {
    display: flex;
    justify-content: center;
    align-items: flex-start;
    min-height: calc(100vh - 80px);
    /* Ajusta para o espaço do header */
    background-color: #f5f5f5;
    /* Fundo cinza claro */
    padding: 20px 0;
}

.how-it-works-box {
    background-color: #FFFFFF;
    padding: 40px;
    border-radius: 10px;
    box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
    width: 100%;
    max-width: 900px;
    /* Ajustado para acomodar o conteúdo */
    text-align: left;
    border: 2px solid #257BB8;
    /* Borda azul */
}

h1 {
    font-size: 2rem;
    color: #257BB8;
    /* Título azul */
    text-align: center;
    margin-bottom: 20px;
}

.intro-text {
    font-size: 1.1rem;
    color: #333;
    margin-bottom: 30px;
    text-align: center;
}

.steps-container {
    display: flex;
    flex-wrap: wrap;
    gap: 20px;
    margin-bottom: 40px;
}

.step-card {
    flex: 1;
    min-width: 200px;
    background-color: #FFFFFF;
    padding: 20px;
    border-radius: 8px;
    border: 1px solid #257BB8;
    /* Borda azul */
    transition: transform 0.2s, box-shadow 0.2s;
}

.step-card:hover {
    transform: translateY(-5px);
    box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
}

.step-number {
    font-size: 1.5rem;
    color: #F4B400;
    /* Amarelo para o número */
    font-weight: bold;
    margin-bottom: 10px;
    text-align: center;
}

.step-card h3 {
    font-size: 1.2rem;
    color: #257BB8;
    /* Título azul */
    margin-bottom: 10px;
}

.step-card p {
    font-size: 1rem;
    color: #666;
}

.benefits-section {
    margin-top: 40px;
}

.benefits-section h2 {
    font-size: 1.5rem;
    color: #257BB8;
    /* Título azul */
    text-align: center;
    margin-bottom: 20px;
}

.benefits-grid {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
    gap: 20px;
}

.benefit-card {
    background-color: #FFFFFF;
    padding: 20px;
    border-radius: 8px;
    border: 1px solid #257BB8;
    /* Borda azul */
    text-align: center;
    transition: transform 0.2s, box-shadow 0.2s;
}

.benefit-card:hover {
    transform: translateY(-5px);
    box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
}

.benefit-icon {
    font-size: 1.5rem;
    color: #F4B400;
    /* Amarelo para o ícone */
    margin-bottom: 10px;
}

.benefit-card h3 {
    font-size: 1.2rem;
    color: #257BB8;
    /* Título azul */
    margin-bottom: 10px;
}

.benefit-card p {
    font-size: 1rem;
    color: #666;
}
</style>