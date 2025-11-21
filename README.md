# 🏷️ HorusTags - Sistema Completo de Tags para Minecraft

[![License](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)
[![Java](https://img.shields.io/badge/Java-8%2B-orange.svg)](https://www.oracle.com/java/)
[![Minecraft](https://img.shields.io/badge/Minecraft-1.13%2B-green.svg)](https://www.minecraft.net/)

**HorusTags** é um sistema completo e profissional de tags para servidores Minecraft, desenvolvido com foco em performance, customização e experiência do usuário.

## 📋 Índice

- [Características Principais](#-características-principais)
- [Funcionalidades](#-funcionalidades)
- [Instalação](#-instalação)
- [Configuração](#-configuração)
- [Comandos](#-comandos)
- [Permissões](#-permissões)
- [Integrações](#-integrações)
- [Banco de Dados](#-banco-de-dados)
- [API](#-api)
- [Suporte](#-suporte)

## ✨ Características Principais

### 🎨 Sistema de Tags Avançado
- **Tags Animadas**: Suporte a animações personalizadas (gradient, rainbow, fade, wave, frame)
- **Efeitos Visuais**: Glow (brilho) e partículas personalizadas
- **Sistema de Raridade**: COMMON, UNCOMMON, RARE, EPIC, LEGENDARY, MYTHIC
- **Categorias**: Organize tags por categorias (staff, ranks, creator, midia, etc.)
- **Prioridade**: Sistema de prioridade para exibição de tags
- **Tags Temporárias**: Tags com duração limitada
- **Tags Limitadas**: Tags com número máximo de proprietários
- **Tags Sazonais**: Tags disponíveis apenas em determinadas estações

### 💰 Sistema Econômico
- **Compra de Tags**: Sistema integrado com Vault para compra de tags
- **Descontos Sazonais**: Sistema de descontos automáticos
- **Reembolsos**: Sistema de reembolso dentro de uma janela de tempo configurável
- **Histórico de Compras**: Registro completo de todas as transações

### 🎁 Sistema Social
- **Troca de Tags**: Sistema de troca entre jogadores
- **Presentear Tags**: Envie tags como presente para outros jogadores
- **Favoritos**: Marque suas tags favoritas para acesso rápido

### 🏆 Sistema de Conquistas
- **Conquistas Personalizadas**: Crie conquistas que desbloqueiam tags
- **Progresso Rastreável**: Sistema de progresso para conquistas
- **Recompensas**: Tags como recompensa por completar conquistas
- **Tipos de Conquistas**: Múltiplos tipos de conquistas suportados

### 🎯 Sistema de Condições
- **Permissões**: Tags baseadas em permissões
- **Grupos Requeridos**: Tags que requerem grupos específicos (LuckPerms)
- **Regiões**: Tags disponíveis apenas em regiões específicas (WorldGuard)
- **Restrições de Tempo**: Tags com horários específicos de uso
- **Conquistas Requeridas**: Tags que requerem conquistas completas

### 🎮 Interface do Usuário
- **GUI Interativa**: Interface gráfica moderna para seleção de tags
- **Modo Chat**: Seleção de tags via chat (alternativa à GUI)
- **Preview de Tags**: Visualize tags antes de equipá-las
- **Sistema de Coleções**: Organize e visualize suas coleções de tags

### 📊 Estatísticas e Análises
- **Estatísticas de Jogadores**: Acompanhe tags possuídas, compradas, etc.
- **Sistema de Logs**: Logs detalhados de todas as ações
- **Backup Automático**: Sistema de backup e restauração
- **Cache Inteligente**: Sistema de cache para melhor performance

### 🎲 Recursos Especiais
- **Tags Aleatórias**: Sistema de tags aleatórias diárias
- **Tags Dinâmicas**: Tags que mudam dinamicamente baseadas em condições
- **Títulos Animados**: Títulos e subtítulos animados ao equipar tags
- **Display Name Customizado**: Personalização do nome de exibição

## 🚀 Funcionalidades Detalhadas

### Sistema de Animações
O HorusTags suporta múltiplos tipos de animações:
- **Frame**: Animação frame por frame
- **Gradient**: Gradiente suave entre cores
- **Rainbow**: Efeito arco-íris animado
- **Fade**: Efeito de fade in/out
- **Wave**: Efeito de onda

### Sistema de Banco de Dados
Suporte para múltiplos tipos de banco de dados:
- **MySQL**: Banco de dados MySQL/MariaDB
- **SQLite/H2**: Banco de dados local SQLite
- **MongoDB**: Banco de dados NoSQL MongoDB
- **FlatFile**: Armazenamento em arquivos YAML

### Sistema de Integrações
Integrações com plugins populares:
- **Vault**: Integração com sistemas econômicos
- **LuckPerms**: Integração com sistema de permissões
- **PlaceholderAPI**: Suporte a placeholders
- **WorldGuard**: Integração com proteção de regiões
- **BungeeCord**: Sincronização entre servidores
- **DiscordSRV**: Integração com Discord

## 📦 Instalação

1. Baixe a versão mais recente do plugin
2. Coloque o arquivo `.jar` na pasta `plugins` do seu servidor
3. Reinicie o servidor
4. Configure o plugin editando os arquivos em `plugins/Horus/`
5. Configure o banco de dados em `plugins/Horus/database.yml`

## ⚙️ Configuração

### Configuração Básica (`config.yml`)

```yaml
# Modo de seleção: "menu" ou "chat"
tag-selection-mode: "chat"

# Cooldown para trocar tags (em milissegundos)
tag-change-cooldown: 5000

# Limite de tags por jogador (-1 para ilimitado)
tag-limit: -1

# Sistema de cache
cache:
  expiry: 300000

# Sistema econômico
economy:
  discounts:
    enabled: true
    seasonal: true
    seasonal-amount: 0.1
  refunds:
    enabled: true
    window: 86400000
```

### Configuração de Banco de Dados (`database.yml`)

```yaml
type: sqlite  # sqlite, mysql, mongodb, flatfile

mysql:
  host: localhost
  port: 3306
  database: horus
  username: root
  password: password

mongodb:
  connection-string: mongodb://localhost:27017
  database: horus
```

## 📝 Comandos

### Comandos do Jogador

| Comando | Descrição | Permissão |
|---------|-----------|-----------|
| `/tag` | Abre o menu de tags ou lista no chat | `horus.use` |
| `/tag set <tag>` | Equipa uma tag | `horus.use` |
| `/tag remove` | Remove a tag atual | `horus.use` |
| `/tag list` | Lista todas as suas tags | `horus.use` |
| `/tag preview <tag>` | Visualiza uma tag | `horus.use` |
| `/tag buy <tag>` | Compra uma tag | `horus.use` |
| `/tag favorite <tag>` | Marca/desmarca uma tag como favorita | `horus.use` |
| `/tag trade <jogador>` | Inicia uma troca de tags | `horus.use` |
| `/tag gift <jogador> <tag>` | Presenteia uma tag | `horus.use` |
| `/tag refund <tag>` | Solicita reembolso de uma tag | `horus.use` |
| `/tag daily` | Recebe tag aleatória diária | `horus.use` |
| `/tag collection` | Visualiza suas coleções | `horus.use` |
| `/tag stats` | Visualiza suas estatísticas | `horus.use` |

### Comandos Administrativos

| Comando | Descrição | Permissão |
|---------|-----------|-----------|
| `/tag create <id>` | Cria uma nova tag | `horus.tag.create` |
| `/tag delete <id>` | Deleta uma tag | `horus.tag.delete` |
| `/tag edit <id>` | Edita uma tag | `horus.tag.create` |
| `/tag give <jogador> <tag>` | Dá uma tag para um jogador | `horus.tag.give` |
| `/tag reload` | Recarrega o plugin | `horus.tag.reload` |
| `/tag backup` | Cria um backup | `horus.admin` |
| `/tag restore <backup>` | Restaura um backup | `horus.admin` |

## 🔐 Permissões

### Permissões Principais

- `horus.*` - Todas as permissões (padrão: OP)
- `horus.use` - Usar o sistema de tags (padrão: true)
- `horus.admin` - Permissões administrativas (padrão: OP)

### Permissões de Tags

- `horus.tag.*` - Todas as permissões de tags
- `horus.tag.<tag-id>` - Permissão para usar uma tag específica
- `horus.tag.create` - Criar tags
- `horus.tag.delete` - Deletar tags
- `horus.tag.give` - Dar tags para jogadores
- `horus.tag.reload` - Recarregar o plugin

## 🔌 Integrações

### Vault
Integração completa com Vault para suporte a múltiplos sistemas econômicos:
- Economia para compra de tags
- Suporte a múltiplos plugins de economia

### LuckPerms
Integração com LuckPerms para:
- Verificação de grupos
- Tags baseadas em grupos
- Sincronização de permissões

### PlaceholderAPI
Suporte completo a placeholders:
- `%horus_tag%` - Tag atual do jogador
- `%horus_tag_prefix%` - Prefixo da tag atual
- `%horus_tag_suffix%` - Sufixo da tag atual
- `%horus_tags_count%` - Número de tags possuídas
- E muitos outros...

### WorldGuard
Integração com WorldGuard para:
- Tags disponíveis apenas em regiões específicas
- Restrições baseadas em localização

### BungeeCord
Sincronização entre servidores:
- Tags sincronizadas em toda a rede
- Dados compartilhados entre servidores

### DiscordSRV
Integração com Discord:
- Notificações no Discord
- Sincronização de eventos

## 💾 Banco de Dados

### Estrutura de Dados

O plugin armazena:
- **Tags**: Todas as tags configuradas
- **Player Tags**: Tags possuídas por cada jogador
- **Achievements**: Conquistas configuradas
- **Player Achievements**: Progresso de conquistas dos jogadores
- **Purchases**: Histórico de compras

### Migração de Banco de Dados

O plugin suporta migração entre diferentes tipos de banco de dados. Basta alterar o tipo no `database.yml` e o plugin migrará automaticamente.

## 🔧 API

### Exemplo de Uso da API

```java
// Obter instância do plugin
Horus plugin = Horus.getInstance();

// Obter tag de um jogador
PlayerTag playerTag = plugin.getPlayerTagManager().getActiveTag(player);

// Criar uma nova tag
Tag tag = new Tag("minha-tag", "Minha Tag");
tag.setPrefix("&6[Minha Tag] &r");
tag.setPrice(1000.0);
plugin.getTagManager().saveTag(tag);

// Dar tag para um jogador
plugin.getPlayerTagManager().giveTag(player, "minha-tag");
```

### Eventos

O plugin dispara os seguintes eventos:
- `TagChangeEvent` - Quando um jogador troca de tag
- `TagPurchaseEvent` - Quando um jogador compra uma tag
- `AchievementCompleteEvent` - Quando um jogador completa uma conquista

## 📈 Performance

- **Cache Inteligente**: Sistema de cache para reduzir consultas ao banco
- **Otimização de Queries**: Queries otimizadas para melhor performance
- **Async Operations**: Operações assíncronas para não travar o servidor
- **Lazy Loading**: Carregamento sob demanda de dados

## 🐛 Suporte

### Problemas Conhecidos

- Certifique-se de que todas as dependências estão instaladas
- Verifique as permissões dos jogadores
- Confirme que o banco de dados está configurado corretamente

### Reportar Bugs

Para reportar bugs, abra uma issue no GitHub com:
- Versão do plugin
- Versão do servidor
- Stack trace completo (se houver)
- Passos para reproduzir o problema

## 📄 Licença

Este projeto está sob a licença MIT. Veja o arquivo `LICENSE` para mais detalhes.

## 👨‍💻 Desenvolvedor

Desenvolvido por **Artix**

## 🙏 Agradecimentos

- Comunidade Minecraft
- Desenvolvedores de plugins de integração
- Todos os contribuidores

---

**HorusTags** - Sistema completo e profissional de tags para Minecraft

Para mais informações, visite: [GitHub Repository](https://github.com/PotDevxs/HorusTags)

