#!/usr/bin/env bash
# ─────────────────────────────────────────────────────────────────────────────
# provision.sh — Provisiona a VM Azure Ubuntu 22.04 para rodar o Phycocarbon
# Execute como: sudo bash provision.sh
# ─────────────────────────────────────────────────────────────────────────────
set -euo pipefail

echo "==> [1/4] Atualizando pacotes..."
apt-get update -y && apt-get upgrade -y

echo "==> [2/4] Instalando Docker..."
apt-get install -y ca-certificates curl gnupg lsb-release git

install -m 0755 -d /etc/apt/keyrings
curl -fsSL https://download.docker.com/linux/ubuntu/gpg \
  | gpg --dearmor -o /etc/apt/keyrings/docker.gpg
chmod a+r /etc/apt/keyrings/docker.gpg

echo \
  "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.gpg] \
  https://download.docker.com/linux/ubuntu $(lsb_release -cs) stable" \
  | tee /etc/apt/sources.list.d/docker.list > /dev/null

apt-get update -y
apt-get install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin

echo "==> [3/4] Configurando Docker para o usuário azureuser..."
usermod -aG docker azureuser
systemctl enable docker
systemctl start docker

echo "==> [4/4] Verificando instalação..."
docker --version
docker compose version

echo ""
echo "    Provisionamento concluído!"
echo "    Faça logout e login novamente para aplicar o grupo docker."
echo "    Depois execute: cd gs1-java-devops/docker && docker compose up -d"
