--
-- PostgreSQL database dump
--

-- Dumped from database version 12.2 (Debian 12.2-2.pgdg100+1)
-- Dumped by pg_dump version 12.2

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

DROP DATABASE IF EXISTS pickaxe_dev;
--
-- Name: pickaxe_dev; Type: DATABASE; Schema: -; Owner: postgres
--

CREATE DATABASE pickaxe_dev WITH TEMPLATE = template0 ENCODING = 'UTF8' LC_COLLATE = 'en_US.utf8' LC_CTYPE = 'en_US.utf8';


ALTER DATABASE pickaxe_dev OWNER TO postgres;

\connect pickaxe_dev

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: games; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.games (
    game character varying NOT NULL,
    week character varying NOT NULL,
    gametime date,
    final boolean DEFAULT false NOT NULL,
    result character varying,
    spread numeric
);


ALTER TABLE public.games OWNER TO postgres;

--
-- Name: userpicks; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.userpicks (
    name character varying NOT NULL,
    week character varying NOT NULL,
    game character varying NOT NULL,
    pick character varying
);


ALTER TABLE public.userpicks OWNER TO postgres;

--
-- Name: users; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.users (
    name character varying NOT NULL,
    active boolean DEFAULT true
);


ALTER TABLE public.users OWNER TO postgres;

--
-- Data for Name: games; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.games (game, week, gametime, final, result, spread) FROM stdin;
GB@CHI	0	\N	f	\N	\N
BUF@NE	0	\N	f	\N	\N
SEA@PHI	0	\N	f	\N	\N
\.


--
-- Data for Name: userpicks; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.userpicks (name, week, game, pick) FROM stdin;
Sereres	0	SEA@PHI	SEA
Sereres	0	GB@CHI	CHI
Vegas	0	GB@CHI	CHI
Vegas	0	SEA@PHI	SEA
RNG	0	GB@CHI	CHI
RNG	0	SEA@PHI	SEA
Seamus	0	GB@CHI	CHI
Seamus	0	SEA@PHI	SEA
RNG	0	BUF@NE	BUF
Vegas	0	BUF@NE	BUF
Seamus	0	BUF@NE	BUF
Sereres	0	BUF@NE	BUF
\.


--
-- Data for Name: users; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.users (name, active) FROM stdin;
Seamus	t
Sereres	t
RNG	t
Vegas	t
\.


--
-- Name: games games_pk; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.games
    ADD CONSTRAINT games_pk PRIMARY KEY (game);


--
-- Name: userpicks userpicks_pk; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.userpicks
    ADD CONSTRAINT userpicks_pk PRIMARY KEY (name, week, game);


--
-- Name: users users_pk; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_pk PRIMARY KEY (name);


--
-- Name: games_game_uindex; Type: INDEX; Schema: public; Owner: postgres
--

CREATE UNIQUE INDEX games_game_uindex ON public.games USING btree (game);


--
-- Name: users_name_uindex; Type: INDEX; Schema: public; Owner: postgres
--

CREATE UNIQUE INDEX users_name_uindex ON public.users USING btree (name);


--
-- Name: userpicks userpicks_games_game_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.userpicks
    ADD CONSTRAINT userpicks_games_game_fk FOREIGN KEY (game) REFERENCES public.games(game);


--
-- Name: userpicks userpicks_users_name_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.userpicks
    ADD CONSTRAINT userpicks_users_name_fk FOREIGN KEY (name) REFERENCES public.users(name);


--
-- PostgreSQL database dump complete
--

