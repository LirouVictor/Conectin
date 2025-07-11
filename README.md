# Conectin

**Conectin** é uma plataforma online desenvolvida como **projeto de conclusão de faculdade** com o objetivo de conectar **trabalhadores informais** a **clientes** em busca de serviços por categoria e região. Com uma interface intuitiva e estrutura full stack, a solução proporciona praticidade na contratação de serviços, avaliação de profissionais e visibilidade para quem oferece mão de obra.

---

## 🧭 Visão Geral

O sistema permite que qualquer visitante visualize livremente os **perfis de prestadores**, suas **categorias**, **cidades atendidas**, **portfólios** (opcional) e **avaliações** recebidas. O **login é exigido apenas para clientes que desejam contratar um profissional**. Após a solicitação, a plataforma envia **notificações automáticas** às partes, possibilitando o controle sobre o status do serviço.

---

## 💡 Funcionalidades

* Cadastro de usuários como `CLIENTE` ou `PRESTADOR`
* Exibição pública de perfis de prestadores com:

  * Categorias atendidas
  * Avaliações
  * Portfólio de trabalhos (opcional)
* Filtro por **categoria** e **cidade(s)**
* Lista ranqueada dos prestadores mais bem avaliados
* Solicitação direta de serviço (apenas clientes logados)
* **Notificações automáticas**:

  * Serviço contratado ou não
  * Serviço concluído ou não
  * Avaliações pendentes
* Sistema de **avaliação mútua**, com nota e comentários

---

## 🔧 Tecnologias

### Back-end

* Java 17
* Spring Boot
* Spring Data JPA
* Hibernate
* PostgreSQL

### Front-end

* Vue.js 3
* Vue Router
* Axios
* HTML5, CSS3
