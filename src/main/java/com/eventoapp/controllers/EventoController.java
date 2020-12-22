package com.eventoapp.controllers;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.eventoapp.models.Convidado;
import com.eventoapp.models.Evento;
import com.eventoapp.repository.ConvidadoRepository;
import com.eventoapp.repository.EventoRepository;

@Controller
public class EventoController {

	@Autowired
	private EventoRepository eventoRepository;

	@Autowired
	private ConvidadoRepository convidadoRepository;

	// Formulário cadastro
	@RequestMapping(value = "/cadastrarEvento", method = RequestMethod.GET)
	public String form() {

		return "evento/formEvento";
	}

	// Cadastro no Banco
	@RequestMapping(value = "/cadastrarEvento", method = RequestMethod.POST)
	public String form(@Valid Evento evento, BindingResult result, RedirectAttributes attributes) {

		if (result.hasErrors()) {
			attributes.addFlashAttribute("mensagem", "Verifique os campos!");
			return "redirect:/cadastrarEvento";

		}
		eventoRepository.save(evento);

		attributes.addFlashAttribute("mensagem", "Evento Cadastrado com sucesso");

		return "redirect:/cadastrarEvento";
	}

	// Listar todos os Eventos
	@RequestMapping("/eventosListar")
	public ModelAndView listaEventos() {

		ModelAndView mv = new ModelAndView("/index");
		Iterable<Evento> eventos = eventoRepository.findAll();
		mv.addObject("eventos", eventos);
		return mv;

	}

	// Exibir Evento Específico - BUSCAR
	@RequestMapping(value = "/{codigo}", method = RequestMethod.GET)
	public ModelAndView detalhesEventos(@PathVariable("codigo") long codigo) {

		Evento evento = eventoRepository.findByCodigo(codigo);
		ModelAndView mv = new ModelAndView("evento/detalhesEvento");
		mv.addObject("evento", evento);

		// criei uma lista com todos os conviados deste evento
		Iterable<Convidado> convidados = convidadoRepository.findByEvento(evento);
		mv.addObject("convidados", convidados);

		return mv;
	}
	
	
	
	//Excluir Convidado
	@RequestMapping("/deletarConvidado")
	public String deletarConvidado(String rg) {
		
		//Primeiro é necessário encontrar o convidado
		Convidado convidado = convidadoRepository.findByRg(rg);
		convidadoRepository.delete(convidado);
		
		//temos que retornar para uma lista dos convidados do evento em que estamos excluindo para isso é necessário o id dp evento
		
		Evento evento = convidado.getEvento();
		long codigoLong = evento.getCodigo();
		
		String codigo = ""+codigoLong;
		return "redirect:/"+codigo;
		
	}
	
	
	
	//Excluir Evento
	@RequestMapping("/deletarEvento")
	public String deletarEvento(long codigo) {
		
		//Encontrar o evento a ser deletado
		Evento evento = eventoRepository.findByCodigo(codigo);
		//É preciso deletar antes dodos os relacionamentos
		Iterable<Convidado> convidados = convidadoRepository.findByEvento(evento);
		convidadoRepository.deleteAll(convidados);
		
		eventoRepository.delete(evento);
		
		return "redirect:/eventosListar";
		
	}

	// Adicionar Conviados ao Evento
	@RequestMapping(value = "/{codigo}", method = RequestMethod.POST)
	public String detalhesEventosPost(@PathVariable("codigo") long codigo, @Valid Convidado convidado,
			BindingResult result, RedirectAttributes attributes) {
		if (result.hasErrors()) {

			attributes.addFlashAttribute("mensagem", "Verifique os campos !");
			return "redirect:/{codigo}";

		}

		Evento evento = eventoRepository.findByCodigo(codigo);
		convidado.setEvento(evento);

		convidadoRepository.save(convidado);
		attributes.addFlashAttribute("mensagem", "Convidado Adicionado com sucesso");

		return "redirect:/{codigo}";
	}

}
