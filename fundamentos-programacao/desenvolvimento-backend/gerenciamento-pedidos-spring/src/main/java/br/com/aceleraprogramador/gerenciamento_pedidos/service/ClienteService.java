package br.com.aceleraprogramador.gerenciamento_pedidos.service;
import br.com.aceleraprogramador.gerenciamento_pedidos.adapter.ClienteAdapter;
import br.com.aceleraprogramador.gerenciamento_pedidos.dto.request.CreateClienteRequest;
import br.com.aceleraprogramador.gerenciamento_pedidos.dto.request.UpdateClienteRequest;
import br.com.aceleraprogramador.gerenciamento_pedidos.dto.response.ClienteResponse;
import br.com.aceleraprogramador.gerenciamento_pedidos.dto.response.PageResponse;
import br.com.aceleraprogramador.gerenciamento_pedidos.exceptions.RecursoNaoEncontradoException;
import br.com.aceleraprogramador.gerenciamento_pedidos.model.Cliente;
import br.com.aceleraprogramador.gerenciamento_pedidos.repository.ClienteRepository;
import br.com.aceleraprogramador.gerenciamento_pedidos.utils.ObjectMapperUtilsConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class ClienteService {

    @Autowired
    ClienteRepository clienteRepository;

    public ClienteResponse criarCliente(CreateClienteRequest createClienteRequest) {

        log.info("Criando um novo cliente...");
        log.info("JSON: {}", ObjectMapperUtilsConfig.pojoParaJson(createClienteRequest));

        Cliente cliente = ClienteAdapter.toCliente(createClienteRequest);
        clienteRepository.save(cliente);
        ClienteResponse clienteResponse = ClienteAdapter.toClienteResponse(cliente);

        log.info("Cliente criado com sucesso...");

        return clienteResponse;
    }

    public PageResponse<ClienteResponse> buscarTodosOsClientes(Pageable pageable) {

        log.info("Buscando todos os clientes...");

        Page<Cliente> clientes = clienteRepository.findAll(pageable);
        Page<ClienteResponse> clienteResponsePage = clientes.map(ClienteAdapter::toClienteResponse);
        PageResponse<ClienteResponse> pageResponse = PageResponse.
                <ClienteResponse>builder()
                .content(clienteResponsePage.getContent())
                .currentPage(clienteResponsePage.getNumber())
                .pageSize(clienteResponsePage.getSize())
                .totalElements(clienteResponsePage.getTotalElements())
                .totalPages(clienteResponsePage.getTotalPages())
                .build();

        log.info("Clientes retornados com sucesso.");

        return pageResponse;
    }

    public ClienteResponse buscarClientePorId(Long idCliente) {
        log.info("Buscando cliente com ID:{}", idCliente);
        Cliente clienteExistente = buscarEntidadeClientePorId(idCliente);
        ClienteResponse clienteResponse = ClienteAdapter.toClienteResponse(clienteExistente);
        log.info("Cliente retornado com sucesso.");
        return clienteResponse;
    }

    public List<ClienteResponse> buscarClientePorNome(String nome) {
        log.info("Buscando cliente com nome:{}", nome);
        List<Cliente> clientesPorNome = clienteRepository.findByNomeContaining(nome);
        List<ClienteResponse> clientesResponse = ClienteAdapter.toClientesResponseList(clientesPorNome);
        log.info("Cliente por nome retornados com sucesso.");
        return clientesResponse;
    }

    public List<ClienteResponse> buscarClientePorEmail(String email) {
        log.info("Buscando cliente com email:{}", email);
        List<Cliente> clientesPorEmail = clienteRepository.findByEmailNative(email);
        List<ClienteResponse> clientesResponse = ClienteAdapter.toClientesResponseList(clientesPorEmail);
        log.info("Cliente por email retornados com sucesso.");
        return clientesResponse;
    }

    public List<ClienteResponse> buscarClientePorProfissao(String profissao) {
        log.info("Buscando cliente com profissao:{}", profissao);
        List<ClienteResponse> clientesResponsePorProfissao = clienteRepository.findClienteResponseByProfissaoNative(profissao);
        log.info("Cliente por profissao retornados com sucesso.");
        return clientesResponsePorProfissao;
    }

    public List<ClienteResponse> buscarClientePorNomeEmailProfissao(String nome, String email, String profissao) {
        log.info("Buscando cliente por nome = " + nome, " email = " + email + " profissao = " + profissao);
        List<Cliente> clientes = clienteRepository.findByNomeEmailProfissao(nome, email, profissao);
        List<ClienteResponse> clientesResponse = ClienteAdapter.toClientesResponseList(clientes);
        log.info("Cliente  retornados com sucesso.");
        return clientesResponse;
    }

    public void atualizarTodosOsDadosDoCliente(Long idCliente, UpdateClienteRequest request) {

        log.info("Atualizando todos os dados do cliente com ID: {}", idCliente);
        log.info("JSON: {}", ObjectMapperUtilsConfig.pojoParaJson(request));

        Cliente clienteExistente = buscarEntidadeClientePorId(idCliente);
        clienteRepository.updateClienteById(clienteExistente.getId(), request.getNome(), request.getEmail(), request.getTelefone(), request.getEndereco(), request.getProfissao());

        log.info("Cliente totalmente atualizado com sucesso.");
    }

    public ClienteResponse atualizarParcialmenteOsDadosDoCliente(Long idCliente, UpdateClienteRequest request) {

        log.info("Atualizando parcialmente os dados do cliente com ID: {}", idCliente);
        log.info("JSON: {}", ObjectMapperUtilsConfig.pojoParaJson(request));

        Cliente clienteExistente = buscarEntidadeClientePorId(idCliente);

        if (StringUtils.isNotBlank(request.getNome())) {
            clienteExistente.setNome(request.getNome());
        }

        if (StringUtils.isNotBlank(request.getEmail())) {
            clienteExistente.setEmail(request.getEmail());
        }

        if (StringUtils.isNotBlank(request.getEndereco())) {
            clienteExistente.setEndereco(request.getEndereco());
        }

        if (StringUtils.isNotBlank(request.getTelefone())) {
            clienteExistente.setTelefone(request.getTelefone());
        }

        if (StringUtils.isNotBlank(request.getProfissao())) {
            clienteExistente.setProfissao(request.getProfissao());
        }

        clienteRepository.save(clienteExistente);
        ClienteResponse clienteResponse = ClienteAdapter.toClienteResponse(clienteExistente);

        log.info("Cliente parcialmente atualizado com sucesso.");

        return clienteResponse;
    }

    public void removerCliente(Long idCliente) {
        log.info("Removendo do cliente com ID: {}", idCliente);
        Cliente clienteExistente = buscarEntidadeClientePorId(idCliente);
        clienteRepository.delete(clienteExistente);
        log.info("Cliente removido com sucesso.");
    }

    private Cliente buscarEntidadeClientePorId(Long idCliente) {
        Optional<Cliente> clienteOptional = clienteRepository.findById(idCliente);
        if (clienteOptional.isEmpty()) {
            String erro = "Cliente não encontrado com o ID: " + idCliente;
            throw new RecursoNaoEncontradoException(erro);
        } else {
            return clienteOptional.get();
        }
    }
}
